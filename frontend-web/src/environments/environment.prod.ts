export const environment = {
    production: true,
    // For Docker: Nginx proxies /api to backend
    apiUrl: '/api',
    stripePublicKey: 'pk_live_YOUR_STRIPE_PUBLIC_KEY_HERE',
    googleClientId: '997621621962-2lr4h9riu1qte8iq8afldtivcm3dgkqe.apps.googleusercontent.com',
    // For Docker: Nginx proxies /ws to backend WebSocket
    websocketUrl: '/ws',
    supportedLanguages: ['en', 'fr'],
    defaultLanguage: 'fr'
};
