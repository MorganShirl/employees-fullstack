import { ProblemDetail } from '@shared/models/problem-detail.model';
import { HttpErrorResponse } from '@angular/common/http';

/**
 * Runtime type guard that verifies whether a value corresponds
 * to a Spring Boot ProblemDetail JSON structure.
 */
const isProblemDetail = (value: unknown): value is ProblemDetail => {
    if (!value || typeof value !== 'object') return false;

    const v = value as Record<string, unknown>;

    if (!('status' in v) || typeof v['status'] !== 'number') return false;
    if (!('title' in v) || typeof v['title'] !== 'string') return false;
    if (!('detail' in v) || typeof v['detail'] !== 'string') return false;

    return true;
};

/**
 * Normalizes any HttpErrorResponse into a consistent Spring ProblemDetail shape.
 * Guarantees that UI components always receive a safe, predictable object.
 */
export function toProblemDetail(err: HttpErrorResponse): ProblemDetail {
    const raw = isProblemDetail(err.error) ? err.error : null;

    const fieldErrors =
        raw && typeof raw['fieldErrors'] === 'object'
            ? (raw['fieldErrors'] as Record<string, string>)
            : undefined;

    return {
        // Spread backend-originating fields if valid
        ...(raw ?? {}),
        status: raw?.status ?? err.status ?? 0,
        title: raw?.title ?? 'Error',
        detail:
            raw?.detail ??
            raw?.title ??
            err.message ??
            'An unexpected error occurred',
        // only attach fieldErrors if present in the error
        ...(fieldErrors ? { fieldErrors } : {}),
    };
}

export function getProblemDetailFromError(err: unknown): ProblemDetail | null {
    if (err instanceof HttpErrorResponse) {
        return err.error as ProblemDetail; // already normalized by interceptor
    }
    return null;
}

/**
 * Builds an array of human-readable lines describing a ProblemDetail.
 *
 * - Always returns multiple lines (string[]), never a single string.
 * - The first lines describe the main ProblemDetail fields (title, detail, status, etc.).
 * - If the backend provides fieldErrors, each field error is added as an additional line.
 *
 * The resulting string[] is suitable for multi-line UI display (e.g., toast, snackbar, dialog).
 */
export function buildProblemDetailLines(problem: ProblemDetail): string[] {
    const lines: string[] = [];

    // Always include the main message
    const mainMessage =
        problem.detail ||
        problem.title ||
        'An unexpected error occurred while calling the API.';
    lines.push(mainMessage);

    // Append field-level validation errors (if any)
    const fieldErrors = problem['fieldErrors'] as Record<string, string> | undefined;

    if (
        fieldErrors &&
        typeof fieldErrors === 'object' &&
        Object.keys(fieldErrors).length > 0
    ) {
        for (const msg of Object.values(fieldErrors)) {
            lines.push(`• ${msg}`);
        }
    }

    return lines;
}
