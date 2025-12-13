export function parseId(idParam: string | null): number | null {
    return idParam !== null && !Number.isNaN(Number(idParam)) ? Number(idParam) : null
}