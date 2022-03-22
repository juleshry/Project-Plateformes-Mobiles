const functions = require("firebase-functions");

// The Firebase Admin SDK to access Firestore.
const admin = require("firebase-admin");
admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions

exports.helloWorld = functions.https.onRequest((request, response) => {
  functions.logger.info("Hello logs!", { structuredData: true });
  response.send("Hello from Firebase!");
});

exports.addUsertoFirestore = functions.auth.user().onCreate((user) => {
  const usersRef = admin.firestore().collection("users");
  return usersRef.doc(user.uid).set({
    email: user.email,
    displayName: user.displayName,
    signinWithGoogle: true,
  });
});
