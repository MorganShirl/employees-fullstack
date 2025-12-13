// simple cookie reader just for this one cookie
export function getCookie(name: string): string | null {
    const match = document.cookie
        .split('; ')
        .find(cookie => cookie.startsWith(name + '='));
    return match ? decodeURIComponent(match.substring(name.length + 1)) : null;
}
