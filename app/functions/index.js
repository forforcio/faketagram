const {onValueCreated} = require("firebase-functions/v2/database");
const {logger} = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.database();

exports.sendChatPushOnMessageCreated = onValueCreated(
    {
      ref: "/messages/{messageId}",
      region: "europe-west1",
    },
    async (event) => {
      const message = event.data.val();
      if (!message) {
        return;
      }

      const receiverUid = message.receiverUid;
      const senderUid = message.senderUid;
      if (!receiverUid || !senderUid) {
        logger.warn("Message without senderUid/receiverUid", {messageId: event.params.messageId});
        return;
      }

      const tokensSnapshot = await db.ref(`fcmTokens/${receiverUid}`).get();
      if (!tokensSnapshot.exists()) {
        logger.info("Receiver has no registered FCM tokens", {receiverUid});
        return;
      }

      const tokenEntries = tokensSnapshot.val() || {};
      const tokens = Object.values(tokenEntries)
          .map((entry) => entry && entry.token)
          .filter((token) => typeof token === "string" && token.length > 0);

      const uniqueTokens = [...new Set(tokens)];
      if (uniqueTokens.length === 0) {
        logger.info("Receiver token list is empty", {receiverUid});
        return;
      }

      const senderName = await resolveSenderName(senderUid);
      const messagePreview = buildMessagePreview(message);
      const senderUserId = await resolveSenderUserId(senderUid);

      const payload = {
        data: {
          senderName,
          senderUserId: String(senderUserId || 0),
          messagePreview,
          messageId: String(event.params.messageId),
        },
        android: {
          priority: "high",
        },
      };

      const response = await admin.messaging().sendEachForMulticast({
        tokens: uniqueTokens,
        ...payload,
      });

      logger.info("FCM notifications sent", {
        receiverUid,
        successCount: response.successCount,
        failureCount: response.failureCount,
      });

      await cleanupInvalidTokens(receiverUid, uniqueTokens, response.responses);
    },
);

function buildMessagePreview(message) {
  if (typeof message.text === "string" && message.text.trim().length > 0) {
    return message.text.trim();
  }
  if (typeof message.imageUrl === "string" && message.imageUrl.trim().length > 0) {
    return "Argazki bat bidali dizu";
  }
  return "Mezu berri bat duzu";
}

async function resolveSenderName(senderUid) {
  const usersSnapshot = await db.ref("users").get();
  if (!usersSnapshot.exists()) {
    return "Mezu berria";
  }

  const users = usersSnapshot.val();
  const sender = Object.values(users || {}).find((user) => user && user.firebaseUid === senderUid);
  return sender && sender.username ? sender.username : "Mezu berria";
}

async function resolveSenderUserId(senderUid) {
  const usersSnapshot = await db.ref("users").get();
  if (!usersSnapshot.exists()) {
    return 0;
  }

  const users = usersSnapshot.val();
  const sender = Object.values(users || {}).find((user) => user && user.firebaseUid === senderUid);
  return sender && sender.userId ? Number(sender.userId) : 0;
}

async function cleanupInvalidTokens(receiverUid, tokens, responses) {
  const removals = [];

  responses.forEach((result, index) => {
    if (result.success) {
      return;
    }

    const code = result.error && result.error.code ? result.error.code : "";
    if (
      code === "messaging/registration-token-not-registered" ||
      code === "messaging/invalid-registration-token"
    ) {
      const tokenToRemove = tokens[index];
      removals.push(removeTokenByValue(receiverUid, tokenToRemove));
    }
  });

  await Promise.all(removals);
}

async function removeTokenByValue(uid, tokenValue) {
  const tokensRef = db.ref(`fcmTokens/${uid}`);
  const snapshot = await tokensRef.get();
  if (!snapshot.exists()) {
    return;
  }

  const entries = snapshot.val() || {};
  const deletions = Object.entries(entries)
      .filter(([, value]) => value && value.token === tokenValue)
      .map(([installationId]) => tokensRef.child(installationId).remove());

  await Promise.all(deletions);
}
