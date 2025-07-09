const AVATAR_COLORS = [
    '#ff6d4d', '#ffbd59', '#0cc0df',
    '#c1ff72', '#5170ff', '#ffde59',
    '#cb6ce6', '#8c52ff', '#ff66c4',
    '#00bf63'
];

const generateColorHash = (username) => {
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
        hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash) % AVATAR_COLORS.length;
};

export const getUserColor = (username, colorMap = {}) => {
    if (!username) return AVATAR_COLORS[0];
    return colorMap[username] || AVATAR_COLORS[generateColorHash(username)];
};

export const initUserColors = (users, currentUser) => {
    const colorMap = {};

    if (currentUser?.username) {
        colorMap[currentUser.username] = getUserColor(currentUser.username);
    }

    users.forEach(user => {
        if (user.username) {
            colorMap[user.username] = getUserColor(user.username);
        }
    });

    return colorMap;
};

// LocalStorage helpers (без хуков)
export const saveColorsToStorage = (colorMap) => {
    try {
        localStorage.setItem('userColors', JSON.stringify(colorMap));
    } catch (e) {
        console.warn('Failed to save colors', e);
    }
};

export const loadColorsFromStorage = () => {
    try {
        const colors = localStorage.getItem('userColors');
        return colors ? JSON.parse(colors) : {};
    } catch (e) {
        console.warn('Failed to load colors', e);
        return {};
    }
};