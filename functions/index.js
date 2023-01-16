const functions = require("firebase-functions");

const admin = require("firebase-admin");
admin.initializeApp();
const database = admin.firestore();

exports.scheduledFunction = functions.pubsub.schedule("0 * * * *")
    .onRun((context) => {
      const moment = require("moment");
      const date = moment(new Date()).subtract(60, "days").toDate();
      const query = database.collection("ads").where("timestamp", "<", date);
      query.get().then(function(querySnapshot) {
        querySnapshot.forEach(function(doc) {
          doc.ref.delete();
        });
      });
      return console.log("successful document delete");
    });

