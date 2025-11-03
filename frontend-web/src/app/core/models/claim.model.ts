export enum ClaimStatus {
    PENDING = 'PENDING',
    IN_PROGRESS = 'IN_PROGRESS',
    RESOLVED = 'RESOLVED',
    REJECTED = 'REJECTED',
    CLOSED = 'CLOSED'
}

export enum ClaimType {
    PRODUCT_DEFECT = 'PRODUCT_DEFECT',
    WRONG_ITEM = 'WRONG_ITEM',
    SHIPPING_DAMAGE = 'SHIPPING_DAMAGE',
    MISSING_PARTS = 'MISSING_PARTS',
    DELIVERY_ISSUE = 'DELIVERY_ISSUE',
    OTHER = 'OTHER'
}

export interface Claim {
    id: number;
    orderId: number;
    orderNumber: string;
    userId: number;
    type: ClaimType;
    subject: string;
    description: string;
    status: ClaimStatus;
    attachments: ClaimAttachment[];
    responses: ClaimResponse[];
    priority: 'LOW' | 'MEDIUM' | 'HIGH';
    createdAt: Date;
    updatedAt: Date;
    resolvedAt?: Date;
}

export interface ClaimAttachment {
    id: number;
    fileName: string;
    fileUrl: string;
    fileType: string;
    fileSize: number;
    uploadedAt: Date;
}

export interface ClaimResponse {
    id: number;
    claimId: number;
    responderId: number;
    responderName: string;
    responderRole: 'CLIENT' | 'ADMIN';
    message: string;
    attachments?: ClaimAttachment[];
    createdAt: Date;
}

export interface CreateClaimRequest {
    orderId: number;
    type: ClaimType;
    subject: string;
    description: string;
    files?: File[];
}

export interface UpdateClaimStatusRequest {
    claimId: number;
    status: ClaimStatus;
    response?: string;
}

export interface AddClaimResponseRequest {
    claimId: number;
    message: string;
    files?: File[];
}
