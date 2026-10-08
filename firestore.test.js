const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

test("Unauthenticated user: cannot read farms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("farms").get());
});

test("Authenticated user: cannot read another user's farms", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("farms").doc("bob_farm_1").set({
      id: "bob_farm_1",
      userId: BOB_UID,
      name: "Bob's Orchard",
      scale: "medium_10acre",
      cropName: "Apples",
      isOrganic: false,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("farms").doc("bob_farm_1").get());
});

test("Authenticated user: can create and read their own farm", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const farmRef = aliceDb.collection("users").doc(ALICE_UID).collection("farms").doc("alice_farm_1");

  await assertSucceeds(
    farmRef.set({
      id: "alice_farm_1",
      userId: ALICE_UID,
      name: "Alice Herb Garden",
      scale: "micro_pot",
      cropName: "Basil",
      isOrganic: true,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(farmRef.get());
});
