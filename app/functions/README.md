# Faketagram Cloud Messaging

Este directorio contiene una Cloud Function que envia notificaciones FCM cuando se crea un mensaje en Realtime Database.

## Flujo

1. Android guarda el token en `fcmTokens/{uid}/{installationId}`.
2. Se crea un mensaje en `messages/{messageId}`.
3. `sendChatPushOnMessageCreated` busca tokens del receptor y envia un push `data` con prioridad alta.

## Requisitos

- Firebase CLI autenticado y apuntando al mismo proyecto.
- Node.js 20.

## Comandos

```bash
cd /home/forforcio/code/projects/Faketagram/app/functions
npm install
npm run lint
npm run deploy
```

## Payload enviado

- `senderName`
- `senderUserId`
- `messagePreview`
- `messageId`

El cliente Android (`ChatFirebaseMessagingService`) consume esos campos y muestra la notificacion mediante `ChatNotificationHelper`.

