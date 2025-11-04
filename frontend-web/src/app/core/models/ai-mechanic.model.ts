export enum DiagnosticType {
    IMAGE = 'IMAGE',
    AUDIO = 'AUDIO',
    CHATBOT = 'CHATBOT'
}

export enum DiagnosticStatus {
    PROCESSING = 'PROCESSING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED'
}

export interface AIDiagnostic {
    id: number;
    userId: number;
    type: DiagnosticType;
    status: DiagnosticStatus;
    inputData: string; // URL for image/audio, or text for chatbot
    results: DiagnosticResult[];
    recommendations: ProductRecommendation[];
    confidence: number;
    createdAt: Date;
    completedAt?: Date;
}

export interface DiagnosticResult {
    diagnosis: string;
    description: string;
    severity: 'LOW' | 'MEDIUM' | 'HIGH';
    confidence: number;
    possibleCauses: string[];
}

export interface ProductRecommendation {
    productId: number;
    productName: string;
    productImage?: string;
    reason: string;
    priority: number;
    price: number;
}

export interface ImageUploadRequest {
    file: File;
    vehicleBrand?: string;
    vehicleModel?: string;
    vehicleYear?: number;
}

export interface AudioUploadRequest {
    file: File;
    duration: number;
    description?: string;
    vehicleBrand?: string;
    vehicleModel?: string;
}

export interface ChatbotMessage {
    id?: number;
    role: 'user' | 'assistant';
    content: string;
    timestamp: Date;
}

export interface ChatbotRequest {
    messages: ChatbotMessage[];
    vehicleInfo?: {
        brand: string;
        model: string;
        year: number;
    };
}

export interface ChatbotResponse {
    message: string;
    suggestions?: string[];
    relatedProducts?: ProductRecommendation[];
    needsMoreInfo: boolean;
}

export interface ImageAnalysisResponse {
    partIdentified: boolean;
    partName?: string;
    confidence: number;
    category?: string;
    recommendations: ProductRecommendation[];
    similarParts?: string[];
}

export interface AudioAnalysisResponse {
    issueDetected: boolean;
    diagnosis: DiagnosticResult[];
    recommendedActions: string[];
    urgencyLevel: 'LOW' | 'MEDIUM' | 'HIGH';
    products: ProductRecommendation[];
}
