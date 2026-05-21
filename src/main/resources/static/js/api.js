export async function fetchThemes() {
    const res = await fetch('/themes');
    if (!res.ok) throw new Error('Failed to fetch themes');
    const data = await res.json();
    return data.themes || data;
}

export async function login(payload) {
    const res = await fetch('/login/web', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({
            loginId: payload.loginId,
            password: payload.password
        })
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to login');
    }
    return res.json();
}

export async function signup(payload) {
    const res = await fetch('/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to signup');
    }
    return res.json();
}

export async function fetchMe() {
    const res = await fetch('/me', { credentials: 'same-origin' });
    if (res.status === 401) {
        return null;
    }
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to fetch me');
    }
    return res.json();
}

export async function requireManager() {
    const me = await fetchMe();
    if (!me) {
        window.location.href = `/auth.html?next=${encodeURIComponent(window.location.pathname)}`;
        return null;
    }
    if (me.role !== 'MANAGER') {
        window.location.href = '/';
        return null;
    }
    return me;
}

export async function logout() {
    const res = await fetch('/logout', {
        method: 'POST',
        credentials: 'same-origin'
    });
    if (!res.ok && res.status !== 204) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to logout');
    }
}

export async function fetchRankedThemes(days, limit) {
    const res = await fetch(`/themes/rank?days=${days}&limit=${limit}`);
    if (!res.ok) throw new Error('Failed to fetch ranked themes');
    const data = await res.json();
    return data.themeRankings || data.themes || data;
}

export async function createTheme(payload) {
    const res = await fetch('/admin/themes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error('Failed to create theme');
    return res.json();
}

export async function deleteTheme(id) {
    const res = await fetch(`/admin/themes/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete theme');
    return res;
}

export async function fetchReservationTimes() {
    const res = await fetch('/reservation-times');
    if (!res.ok) throw new Error('Failed to fetch times');
    const data = await res.json();
    return data.reservationTimes || data;
}

export async function fetchAvailableTimes(storeId, themeId, date) {
    const res = await fetch(`/reservation-times/available?storeId=${storeId}&themeId=${themeId}&date=${date}`);
    if (!res.ok) throw new Error('Failed to fetch available times');
    const data = await res.json();
    return data.availableTimes || data;
}

export async function createReservationTime(startAt) {
    const res = await fetch('/admin/reservation-times', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startAt })
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create time');
    }
    return res.json();
}

export async function deleteReservationTime(id) {
    const res = await fetch(`/admin/reservation-times/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete time');
    return res;
}

export async function createReservation(payload) {
    const res = await fetch('/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to create reservation');
    }
    return res.json();
}

export async function fetchMyReservations(page = 0, size = 20) {
    const res = await fetch(`/reservations?page=${page}&size=${size}`, {
        credentials: 'same-origin'
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to fetch my reservations');
    }
    const data = await res.json();
    return data.reservations || data;
}

export async function updateReservationSchedule(id, payload) {
    const res = await fetch(`/reservations/${id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to update schedule');
    }
    return res.json();
}

export async function cancelMyReservation(id) {
    const res = await fetch(`/reservations/${id}`, {
        method: 'DELETE',
        credentials: 'same-origin'
    });
    if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message || 'Failed to cancel reservation');
    }
    return res.json();
}

export async function fetchAdminReservations(page = 0, size = 100) {
    const res = await fetch(`/admin/reservations?page=${page}&size=${size}`);
    if (!res.ok) throw new Error('Failed to fetch admin reservations');
    const data = await res.json();
    return data.reservations || data;
}

export async function deleteAdminReservation(id) {
    const res = await fetch(`/admin/reservations/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Failed to delete admin reservation');
    return res;
}
