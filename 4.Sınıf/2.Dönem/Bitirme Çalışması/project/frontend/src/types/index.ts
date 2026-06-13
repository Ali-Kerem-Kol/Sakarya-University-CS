export type Role = 'USER' | 'ADMIN';

export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED' | 'REMOVED';
export type PostingStatus = 'DRAFT' | 'PUBLISHED' | 'CLOSED';
export type PostingCategory = 'BACKEND' | 'FRONTEND' | 'MOBILE' | 'FULLSTACK';
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

// Task Status
export type TaskStatus = 'Assigned' | 'Done' | 'Failed' | 'In Progress'; // Assuming these, but 'ASSIGNED/DONE/FAILED' requested

export interface User {
    id: string;
    email: string;
    role: Role;
    firstName?: string;
    lastName?: string;
}

export interface Attachment {
    id: string;
    originalFileName: string;
    contentType: string;
    size: number;
    downloadUrl: string;
}

export interface Posting {
    id: string;
    title: string;
    description: string;
    projectName: string;
    projectDetails: string;
    category: PostingCategory;
    status: PostingStatus;
    attachments: Attachment[];
    createdAt: string;
    updatedAt: string;
}

// Announcements
export interface Announcement {
    id: string;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    isPublished: boolean;
}

// Tasks
export interface ProjectTask {
    id: string;
    postingId: string;
    title: string;
    description: string;
    assignedUserId?: string; // or assignedUser object
    assignedUserEmail?: string;
    status: TaskStatus;
    dueDate?: string;
    createdAt: string;
}

// Q&A
export type QuestionScope = 'PRIVATE' | 'PROJECT_ONLY' | 'PUBLIC';
export type AdminQuestionStatusFilter = 'ANSWERED' | 'UNANSWERED' | 'PUBLISHED' | 'UNPUBLISHED';

export interface PublicQuestion {
    id: string | number;
    postingId: string | number;
    questionText: string;
    answerText: string | null;
    publishedAt: string | null;
}

export interface StudentQuestion {
    id: string | number;
    postingId: string | number;
    questionText: string;
    createdAt: string;
    answeredAt: string | null;
    answerText: string | null;
    isPublished: boolean;
}

export interface AdminQuestion {
    id: string | number;
    postingId: string | number;
    postingTitle: string;
    questionText: string;
    createdAt: string;
    askedByUserId: string | number;
    askedByEmail: string;
    askedByName: string;
    answerText: string | null;
    isPublished: boolean;
    publishedAt: string | null;
}

export interface AuthResponse {
    accessToken: string;
    tokenType: string;
    expiresAt: string;
    userId: string;
    email: string;
    role: Role;
    firstName?: string;
    lastName?: string;
}

export interface ApiErrorResponse {
    errorCode: string;
    message: string;
    path: string;
    timestamp: string;
}

export interface UserProfile {
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber?: string;
    githubUrl?: string;
    linkedinUrl?: string;
    gpa?: number;
    classYear?: number;
    department?: string;
    englishLevel?: string;
}

export interface ProjectDocument {
    id: string;
    fileName: string;
    fileType: string;
    documentType: 'CV';
    createdAt: string;
}

export interface AvailabilitySlot {
    id: string;
    dayOfWeek: DayOfWeek;
    startTime: string; // "HH:mm"
    endTime: string;   // "HH:mm"
}

export interface Application {
    id: string;
    userId: string;
    postingId?: string;
    email: string;
    firstName?: string;
    lastName?: string;
    positionKey: string;
    motivationText?: string;
    status: ApplicationStatus;
    createdAt: string;
    updatedAt: string;
    lastStatusChangedAt?: string;
    postingTitle?: string;
}

export interface ApplicantOverviewResponse {
    userId: string;
    email: string;
    firstName?: string;
    lastName?: string;
    latestApplicationId?: string;
    latestStatus?: ApplicationStatus;
    latestPositionKey?: string;
    latestLastStatusChangedAt?: string;
    hasCv: boolean;
    hasAvailability: boolean;
    availabilitySlotCount: number;
}

export interface ApplicationNote {
    id: string;
    noteText: string;
    createdBy: string;
    createdAt: string;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
}

export type PaginatedArray<T> = T[] & {
    totalElements?: number;
    totalPages?: number;
    size?: number;
    number?: number;
};
