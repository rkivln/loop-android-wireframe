const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated bounds
test("Unauthenticated user: cannot read user profile or list items", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("items").get());
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("conversations").get());
});

test("Unauthenticated user: cannot create rental item", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("items").doc("item_1").set({
    id: "item_1",
    title: "Canon DSLR",
    categoryName: "ELECTRONICS",
    pricePerDay: 500,
    ownerId: ALICE_UID,
    createdAt: new Date(),
  }));
});

// 2. User profiles
test("Authenticated user: can create and update their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    email: "alice@test.com",
    createdAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).update({
    bio: "Film shooter in White Town",
    favoriteItemIds: ["item_100"],
    appearanceMode: "DARK",
    profileVisibility: "PRIVATE",
    locationSharing: "NEIGHBORHOOD_ONLY",
    notifyPush: true,
    notifyNewPosts: false,
    notifyCommentReplies: true,
    notifyLikes: false,
    notifyAnnouncements: true,
    blockedUserIds: [BOB_UID],
    blockedUsers: [{ userId: BOB_UID, displayName: "Bob", blockedAtLabel: "Today" }],
    fcmToken: "fcm_device_token_xyz_12345",
    fcmTokenUpdatedAt: new Date(Date.now() - 1000),
    updatedAt: new Date(),
  }));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({
    appearanceMode: "NEON_MODE",
    updatedAt: new Date(),
  }));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({
    fcmToken: "x".repeat(600),
    updatedAt: new Date(),
  }));
});

test("Authenticated user: cannot create or update someone else's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    displayName: "Bob",
    email: "bob@test.com",
    createdAt: new Date(),
  }));
});

// 3. Rental items
test("Authenticated user: can create and update rental item they own, but not someone else's", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("items").doc("item_100").set({
    id: "item_100",
    title: "Sony Alpha A7 IV",
    categoryName: "ELECTRONICS",
    pricePerDay: 850,
    securityDeposit: 2000,
    ownerId: ALICE_UID,
    createdAt: new Date(),
  }));

  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("items").doc("bob_item").set({
      id: "bob_item",
      title: "Bob's Scooter",
      categoryName: "VEHICLES",
      pricePerDay: 300,
      ownerId: BOB_UID,
      createdAt: new Date(),
    });
  });

  await assertSucceeds(aliceDb.collection("items").doc("item_multi").set({
    id: "item_multi",
    title: "Multi Photo Tripod Kit",
    categoryName: "ELECTRONICS",
    pricePerDay: 450,
    imageUrl: "data:image/jpeg;base64,AAA",
    imageUrls: [
      "data:image/jpeg;base64,AAA",
      "data:image/jpeg;base64,BBB",
      "data:image/jpeg;base64,CCC",
      "data:image/jpeg;base64,DDD"
    ],
    imageCount: 4,
    ownerId: ALICE_UID,
    createdAt: new Date(),
  }));

  await assertFails(aliceDb.collection("items").doc("item_too_many").set({
    id: "item_too_many",
    title: "Too Many Photos",
    categoryName: "ELECTRONICS",
    pricePerDay: 450,
    imageUrls: ["1", "2", "3", "4", "5"],
    imageCount: 5,
    ownerId: ALICE_UID,
    createdAt: new Date(),
  }));

  await assertFails(aliceDb.collection("items").doc("bob_item").update({
    title: "Alice Hacked Scooter",
    updatedAt: new Date(),
  }));

  // Non-owner cannot delete another user's active item
  await assertFails(aliceDb.collection("items").doc("bob_item").delete());

  // Legacy seeded item (pre-cutoff or seed_ owner) can be purged by signed-in user
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("items").doc("seed_item_1").set({
      id: "seed_item_1",
      title: "Legacy Seed Item",
      categoryName: "ELECTRONICS",
      pricePerDay: 500,
      ownerId: "seed_vikram",
      createdAt: new Date("2026-11-10T10:00:00Z"),
    });
  });
  await assertSucceeds(aliceDb.collection("items").doc("seed_item_1").delete());
});

// 4. Community posts
test("Authenticated user: can create community post, another member can RSVP, non-author cannot delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("community_posts").doc("post_1").set({
    id: "post_1",
    title: "French Quarter Photowalk",
    typeStr: "EVENT",
    statusStr: "ACTIVE",
    authorId: ALICE_UID,
    createdAt: new Date(),
  }));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("community_posts").doc("post_1").update({
    attendeesOrResponses: "1 creator attending",
    attendeeIds: [BOB_UID],
    updatedAt: new Date(),
  }));

  // Non-author cannot alter title or delete active post
  await assertFails(bobDb.collection("community_posts").doc("post_1").update({
    title: "Hacked Post",
    updatedAt: new Date(),
  }));
  await assertFails(bobDb.collection("community_posts").doc("post_1").delete());

  // Legacy seeded post can be purged
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("community_posts").doc("seed_post_1").set({
      id: "seed_post_1",
      title: "Legacy Seed Photowalk",
      typeStr: "EVENT",
      statusStr: "ACTIVE",
      authorId: "seed_host",
      createdAt: new Date("2026-11-10T10:00:00Z"),
    });
  });
  await assertSucceeds(bobDb.collection("community_posts").doc("seed_post_1").delete());
});

// 5. Conversations & Messages (participant-scoped)
test("Conversation participants: can query own conversations, read and send immutable messages; self-chat rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Self-conversation must fail
  await assertFails(aliceDb.collection("conversations").doc("conv_self").set({
    id: "conv_self",
    participantIds: [ALICE_UID],
    hostId: ALICE_UID,
    renterId: ALICE_UID,
    createdAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").set({
    id: "conv_1",
    participantIds: [ALICE_UID, BOB_UID],
    hostId: BOB_UID,
    renterId: ALICE_UID,
    createdAt: new Date(),
  }));

  // Participant query with array-contains must succeed
  await assertSucceeds(
    aliceDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get()
  );

  // Unscoped list must fail (rules are not filters)
  await assertFails(aliceDb.collection("conversations").get());

  // Send message
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").set({
    id: "msg_1",
    senderId: ALICE_UID,
    text: "Hi Bob, is the scooter available tomorrow?",
    messageTypeStr: "TEXT",
    statusStr: "SENT",
    createdAt: new Date(),
  }));

  // Messages are append-only
  await assertFails(
    aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_1").update({
      text: "Edited",
    })
  );

  // Encrypted message with reply, coordinates, and disappearing timer succeeds
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_e2ee").set({
    id: "msg_e2ee",
    senderId: ALICE_UID,
    text: "Encrypted message",
    messageTypeStr: "LOCATION_PIN",
    statusStr: "SENT",
    isEncrypted: true,
    keyVersion: 1,
    ciphertext: "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXo=",
    iv: "MTIzNDU2Nzg5MDEy",
    senderPublicKey: "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE",
    recipientPublicKey: "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAF",
    replyToMessageId: "msg_1",
    replyToSenderName: "Alice",
    latitude: 11.934,
    longitude: 79.830,
    expiresAtEpoch: Date.now() + 86400000,
    createdAt: new Date(),
  }));

  // Oversized ciphertext must fail
  await assertFails(aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_huge").set({
    id: "msg_huge",
    senderId: ALICE_UID,
    text: "Encrypted message",
    messageTypeStr: "IMAGE_MEDIA",
    statusStr: "SENT",
    isEncrypted: true,
    ciphertext: "a".repeat(460000),
    iv: "MTIzNDU2Nzg5MDEy",
    createdAt: new Date(),
  }));

  // Conversation mute & disappearing timer updates succeed for participant
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").update({
    mutedByUserIds: [ALICE_UID],
    disappearingTimerSeconds: 86400,
    e2eeEnabled: true,
    updatedAt: new Date(),
  }));

  // Invalid disappearing timer duration fails
  await assertFails(aliceDb.collection("conversations").doc("conv_1").update({
    disappearingTimerSeconds: 999999999,
    updatedAt: new Date(),
  }));

  // Only sender can delete their own message
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_e2ee").delete());
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_1").collection("messages").doc("msg_e2ee").delete());
});

test("User E2EE public key and chat privacy settings validation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    email: "alice@test.com",
    createdAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).update({
    e2eePublicKey: "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEtestKey",
    e2eeKeyFingerprint: "A1B2 C3D4 E5F6 0718",
    sendReadReceipts: false,
    showTypingStatus: false,
    defaultDisappearingSeconds: 604800,
    hideNotificationPreviews: true,
    updatedAt: new Date(),
  }));

  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({
    defaultDisappearingSeconds: -10,
    updatedAt: new Date(),
  }));
});

test("Non-participant: cannot read conversation or list/write messages", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_secret").set({
      id: "conv_secret",
      participantIds: [CHARLIE_UID, BOB_UID],
      hostId: BOB_UID,
      renterId: CHARLIE_UID,
      createdAt: new Date(),
    });
    await context.firestore().collection("conversations").doc("conv_secret").collection("messages").doc("msg_s").set({
      id: "msg_s",
      senderId: BOB_UID,
      text: "Secret handover PIN",
      messageTypeStr: "TEXT",
      statusStr: "SENT",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("conversations").doc("conv_secret").get());
  await assertFails(aliceDb.collection("conversations").doc("conv_secret").collection("messages").get());
  await assertFails(
    aliceDb.collection("conversations").doc("conv_secret").collection("messages").doc("msg_x").set({
      id: "msg_x",
      senderId: ALICE_UID,
      text: "Intrusion",
      messageTypeStr: "TEXT",
      statusStr: "SENT",
      createdAt: new Date(),
    })
  );
});

// 6. Bookings & Item Booking Lock + Reviews
test("Bookings: self-booking rejected, renter locks & reviews item, host transitions status, third party rejected", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("items").doc("bob_cam").set({
    id: "bob_cam",
    title: "Bob's Leica",
    categoryName: "ELECTRONICS",
    pricePerDay: 900,
    ownerId: BOB_UID,
    availableToday: true,
    rating: 0,
    reviewCount: 0,
    createdAt: new Date(),
  }));

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Self-booking rejected
  await assertFails(aliceDb.collection("bookings").doc("book_self").set({
    id: "book_self",
    renterId: ALICE_UID,
    hostId: ALICE_UID,
    itemId: "bob_cam",
    totalAmount: 950,
    status: "Confirmed",
    handoverOtp: "1234",
    createdAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("bookings").doc("book_1").set({
    id: "book_1",
    renterId: ALICE_UID,
    hostId: BOB_UID,
    itemId: "bob_cam",
    totalAmount: 1850,
    status: "Confirmed",
    handoverOtp: "4821",
    createdAt: new Date(),
  }));

  // Renter can lock Bob's item with valid activeBookingId
  await assertSucceeds(aliceDb.collection("items").doc("bob_cam").update({
    availableToday: false,
    activeBookingId: "book_1",
    updatedAt: new Date(),
  }));

  // Charlie cannot lock Bob's item with Alice's booking
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("items").doc("bob_cam").update({
    availableToday: false,
    activeBookingId: "book_1",
    updatedAt: new Date(),
  }));

  await assertSucceeds(aliceDb.collection("bookings").where("renterId", "==", ALICE_UID).get());
  await assertFails(aliceDb.collection("bookings").get());

  // Host transitions Confirmed -> Active -> Completed and unlocks item
  await assertSucceeds(bobDb.collection("bookings").doc("book_1").update({
    status: "Active",
    updatedAt: new Date(),
  }));
  await assertSucceeds(bobDb.collection("bookings").doc("book_1").update({
    status: "Completed",
    updatedAt: new Date(),
  }));
  await assertSucceeds(bobDb.collection("items").doc("bob_cam").update({
    availableToday: true,
    activeBookingId: null,
    updatedAt: new Date(),
  }));

  // Borrower can submit review & increment item rating
  await assertSucceeds(aliceDb.collection("bookings").doc("book_1").update({
    isReviewed: true,
    ratingGiven: 5,
    reviewComment: "Clean lens and smooth pickup",
    updatedAt: new Date(),
  }));
  await assertSucceeds(aliceDb.collection("items").doc("bob_cam").update({
    rating: 5.0,
    reviewCount: 1,
    updatedAt: new Date(),
  }));

  // Out-of-bounds rating rejected
  await assertFails(aliceDb.collection("items").doc("bob_cam").update({
    rating: 9.0,
    reviewCount: 2,
    updatedAt: new Date(),
  }));

  // Third party rejected
  await assertFails(charlieDb.collection("bookings").doc("book_1").get());
  await assertFails(charlieDb.collection("bookings").doc("book_1").update({
    status: "Cancelled",
    updatedAt: new Date(),
  }));
});

test("Bookings: atomic batch create+lock succeeds via getAfter and prevents double-booking overwrite", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("items").doc("atomic_cam").set({
    id: "atomic_cam",
    title: "Fujifilm X100VI",
    categoryName: "ELECTRONICS",
    pricePerDay: 950,
    ownerId: BOB_UID,
    availableToday: true,
    activeBookingId: null,
    rating: 0,
    reviewCount: 0,
    createdAt: new Date(),
  }));

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceBatch = aliceDb.batch();
  aliceBatch.set(aliceDb.collection("bookings").doc("book_atomic_1"), {
    id: "book_atomic_1",
    renterId: ALICE_UID,
    hostId: BOB_UID,
    itemId: "atomic_cam",
    totalAmount: 1950,
    status: "Confirmed",
    handoverOtp: "2468",
    createdAt: new Date(),
  });
  aliceBatch.update(aliceDb.collection("items").doc("atomic_cam"), {
    availableToday: false,
    activeBookingId: "book_atomic_1",
    updatedAt: new Date(),
  });
  await assertSucceeds(aliceBatch.commit());

  // Second renter Charlie cannot overwrite an already-locked item in a concurrent batch
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const charlieBatch = charlieDb.batch();
  charlieBatch.set(charlieDb.collection("bookings").doc("book_atomic_2"), {
    id: "book_atomic_2",
    renterId: CHARLIE_UID,
    hostId: BOB_UID,
    itemId: "atomic_cam",
    totalAmount: 1950,
    status: "Confirmed",
    handoverOtp: "1357",
    createdAt: new Date(),
  });
  charlieBatch.update(charlieDb.collection("items").doc("atomic_cam"), {
    availableToday: false,
    activeBookingId: "book_atomic_2",
    updatedAt: new Date(),
  });
  await assertFails(charlieBatch.commit());

  // Renter Alice cancels confirmed booking and unlocks item in an atomic batch
  const cancelBatch = aliceDb.batch();
  cancelBatch.update(aliceDb.collection("bookings").doc("book_atomic_1"), {
    status: "Cancelled",
    updatedAt: new Date(),
  });
  cancelBatch.update(aliceDb.collection("items").doc("atomic_cam"), {
    availableToday: true,
    activeBookingId: null,
    updatedAt: new Date(),
  });
  await assertSucceeds(cancelBatch.commit());
});

// 6. Long-term monthly house rentals (/houses) & visit requests (/visit_requests)
test("Unauthenticated user: cannot read or write houses or visit_requests", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("houses").get());
  await assertFails(unauthDb.collection("visit_requests").get());
  await assertFails(unauthDb.collection("houses").doc("house_unauth").set({
    id: "house_unauth",
    title: "2 BHK Heritage Home",
    propertyType: "APARTMENT",
    monthlyRent: 22000,
    ownerId: ALICE_UID,
    createdAt: new Date(Date.now() - 1000),
  }));
});

test("House listings: landlord can create, update, toggle favorites, and non-owners cannot edit or delete", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Invalid monthly rent below 1000 rejected
  await assertFails(bobDb.collection("houses").doc("house_low_rent").set({
    id: "house_low_rent",
    title: "Too Cheap Room",
    propertyType: "STUDIO",
    monthlyRent: 500,
    ownerId: BOB_UID,
    createdAt: now,
  }));

  // Valid Kotlin-aligned house payload succeeds
  await assertSucceeds(bobDb.collection("houses").doc("house_1").set({
    id: "house_1",
    title: "Sunlit 2 BHK Courtyard Apartment",
    propertyType: "APARTMENT",
    bhkCount: 2,
    bathrooms: 2,
    areaSqFt: 1120,
    floorNumber: 1,
    totalFloors: 3,
    furnishing: "SEMI_FURNISHED",
    tenantPreference: "ANY",
    monthlyRent: 24000,
    securityDeposit: 75000,
    maintenanceMonthly: 1500,
    minLeaseMonths: 11,
    availableFromLabel: "Immediate",
    isAvailableForRent: true,
    location: "White Town, Puducherry",
    description: "Airy French-quarter inspired 2 BHK near Promenade.",
    amenities: ["WIFI", "PARKING_BIKE", "BALCONY"],
    imageUrls: ["data:image/jpeg;base64,test"],
    photoLabels: ["Living Room"],
    imageCount: 1,
    ownerId: BOB_UID,
    ownerName: "Bob",
    ownerInitials: "BO",
    ownerBadge: "Verified Host",
    rating: 0.0,
    reviewCount: 0,
    createdAt: now,
    updatedAt: now,
  }));

  // Landlord can update rent & availability
  await assertSucceeds(bobDb.collection("houses").doc("house_1").update({
    monthlyRent: 23000,
    isAvailableForRent: false,
    updatedAt: new Date(Date.now() - 500),
  }));

  // Shadow update with extra/immutable field rejected
  await assertFails(bobDb.collection("houses").doc("house_1").update({
    ownerId: ALICE_UID,
    updatedAt: new Date(Date.now() - 500),
  }));

  // Non-owner Alice cannot edit or delete Bob's house
  await assertFails(aliceDb.collection("houses").doc("house_1").update({
    monthlyRent: 12000,
    updatedAt: new Date(Date.now() - 500),
  }));
  await assertFails(aliceDb.collection("houses").doc("house_1").delete());

  // Alice can bookmark favoriteHouseIds on her profile
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice",
    email: "alice@test.com",
    favoriteHouseIds: ["house_1"],
    createdAt: now,
  }));
});

test("House visit requests: dual-party scoping, state transitions, and third-party isolation", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(bobDb.collection("houses").doc("house_visit_1").set({
    id: "house_visit_1",
    title: "3 BHK Villa near Auroville Road",
    propertyType: "VILLA",
    bhkCount: 3,
    monthlyRent: 36000,
    securityDeposit: 100000,
    ownerId: BOB_UID,
    ownerName: "Bob",
    isAvailableForRent: true,
    createdAt: now,
    updatedAt: now,
  }));

  // Self-visit request by landlord fails
  await assertFails(bobDb.collection("visit_requests").doc("visit_self").set({
    id: "visit_self",
    houseId: "house_visit_1",
    tenantId: BOB_UID,
    landlordId: BOB_UID,
    preferredDate: "2026-12-01",
    preferredTimeSlot: "MORNING",
    status: "REQUESTED",
    createdAt: now,
  }));

  // Creating visit for non-existent house fails
  await assertFails(aliceDb.collection("visit_requests").doc("visit_orphan").set({
    id: "visit_orphan",
    houseId: "missing_house",
    tenantId: ALICE_UID,
    landlordId: BOB_UID,
    preferredDate: "2026-12-01",
    preferredTimeSlot: "MORNING",
    status: "REQUESTED",
    createdAt: now,
  }));

  // Valid parallel visit requests from Alice and Charlie succeed
  await assertSucceeds(aliceDb.collection("visit_requests").doc("visit_alice").set({
    id: "visit_alice",
    houseId: "house_visit_1",
    houseTitle: "3 BHK Villa near Auroville Road",
    houseLocation: "Auroville Road",
    monthlyRent: 36000,
    tenantId: ALICE_UID,
    tenantName: "Alice",
    landlordId: BOB_UID,
    landlordName: "Bob",
    preferredDate: "2026-12-01",
    preferredTimeSlot: "AFTERNOON",
    occupantsCount: 2,
    householdType: "FAMILY",
    targetMoveInMonth: "January 2027",
    tenantNote: "Looking for a quiet long-term 24-month lease.",
    status: "REQUESTED",
    createdAt: now,
    updatedAt: now,
  }));

  await assertSucceeds(charlieDb.collection("visit_requests").doc("visit_charlie").set({
    id: "visit_charlie",
    houseId: "house_visit_1",
    tenantId: CHARLIE_UID,
    landlordId: BOB_UID,
    preferredDate: "2026-12-02",
    preferredTimeSlot: "EVENING",
    status: "REQUESTED",
    createdAt: now,
    updatedAt: now,
  }));

  // Scoped queries succeed while unfiltered list query is denied
  await assertSucceeds(
    aliceDb.collection("visit_requests").where("tenantId", "==", ALICE_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("visit_requests").where("landlordId", "==", BOB_UID).get()
  );
  await assertFails(aliceDb.collection("visit_requests").get());

  // Third party Charlie cannot read or update Alice's visit request
  await assertFails(charlieDb.collection("visit_requests").doc("visit_alice").get());
  await assertFails(charlieDb.collection("visit_requests").doc("visit_alice").update({
    status: "CANCELLED",
    updatedAt: new Date(Date.now() - 500),
  }));

  // Tenant Alice cannot self-confirm her own visit request
  await assertFails(aliceDb.collection("visit_requests").doc("visit_alice").update({
    status: "CONFIRMED",
    updatedAt: new Date(Date.now() - 500),
  }));

  // Landlord Bob can confirm Alice's visit with address note
  await assertSucceeds(bobDb.collection("visit_requests").doc("visit_alice").update({
    status: "CONFIRMED",
    landlordResponseNote: "See you at 3 PM.",
    confirmedAddressNote: "Gate 14, opposite Botanical Corner.",
    updatedAt: new Date(Date.now() - 500),
  }));

  // Landlord Bob can decline Charlie's visit, then terminal lock blocks further updates
  await assertSucceeds(bobDb.collection("visit_requests").doc("visit_charlie").update({
    status: "DECLINED",
    landlordResponseNote: "Slot taken.",
    updatedAt: new Date(Date.now() - 500),
  }));
  await assertFails(charlieDb.collection("visit_requests").doc("visit_charlie").update({
    status: "REQUESTED",
    updatedAt: new Date(Date.now() - 200),
  }));

  // House conversation context in Studio Dispatch succeeds
  await assertSucceeds(aliceDb.collection("conversations").doc("conv_house_1").set({
    id: "conv_house_1",
    participantIds: [ALICE_UID, BOB_UID],
    hostId: BOB_UID,
    hostName: "Bob",
    renterId: ALICE_UID,
    renterName: "Alice",
    houseId: "house_visit_1",
    houseTitle: "3 BHK Villa near Auroville Road",
    houseMonthlyRent: 36000,
    lastMessageText: "Hi Bob, asking about the garden villa.",
    lastMessageTimestamp: "Just now",
    createdAt: now,
    updatedAt: now,
  }));
});


