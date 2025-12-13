export interface ProblemDetail {
    status: number;
    title: string;
    detail: string;
    [key: string]: unknown; // allow adding arbitrary properties to an object that implements that interface (such as fieldErrors).
}
