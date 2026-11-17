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
    updatedAt: new Date(),
  }));
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).update({
    appearanceMode: "NEON_MODE",
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

