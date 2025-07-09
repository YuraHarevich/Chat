import React, { useState, useEffect, useRef, useContext } from 'react';
import { Client } from '@stomp/stompjs';
import './styles/Home.css';
import { AuthContext } from "./auth/AuthProvider";

import {getUserColor, initUserColors, loadColorsFromStorage, saveColorsToStorage} from './style-code/avatar';

const Home = () => {
    const WS_URL = process.env.REACT_APP_WS_URL || 'ws://localhost:8000/ws-chat';
    const {
        user,
        loading: authLoading,
        logout,
        makeAuthRequest,
        apiAxios
    } = useContext(AuthContext);
    const [contacts, setContacts] = useState([]);
    const [messages, setMessages] = useState([]);
    const [messageInput, setMessageInput] = useState('');
    const [sidebarActive, setSidebarActive] = useState(false);
    const [selectedChat, setSelectedChat] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isInitialized, setIsInitialized] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [messagePage, setMessagePage] = useState(0);
    const [hasMoreMessages, setHasMoreMessages] = useState(true);
    const [stompClient, setStompClient] = useState(null);
    const [showSettingsModal, setShowSettingsModal] = useState(false);
    const [showProfileModal, setShowProfileModal] = useState(false);
    const [currentTheme, setCurrentTheme] = useState('dark');
    const [viewedUser, setViewedUser] = useState(null);
    const [showUserProfile, setShowUserProfile] = useState(false);
    const [showOptions, setShowOptions] = useState(false);

    const contactsListRef = useRef(null);
    const messagesEndRef = useRef(null);
    const messagesContainerRef = useRef(null);

    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [showSearchResults, setShowSearchResults] = useState(false);
    const [searchTimeout, setSearchTimeout] = useState(null);

    const themes = {
        dark: {
            '--main-color': '#1f1f1f',
            '--second-main-color': '#2b2b2b',
            '--supporting-color': '#1f1f1f',
            '--chat-text-color': '#ffffff',
            '--message-text-color': '#ffffff',
            '--message-bg-color': '#ecaf7c',
            '--border-color': '#878787',
            '--selected-chat-color': '#d4727d',
            '--chat-hover-color': '#1f1f1f',
        },
        light: {
            '--main-color': '#e4edf0',
            '--second-main-color': '#ffffff',
            '--supporting-color': '#d1dfe4',
            '--chat-text-color': '#000000',
            '--message-text-color': '#ffffff',
            '--message-bg-color': '#b2bec2',
            '--border-color': '#b4b4b4',
            '--selected-chat-color': '#d1dfe4',
            '--chat-hover-color': '#e7eced',
        }
    };

    const iconPaths = {
        dark: {
            profile: require('./icons/theme/dark/profile.png'),
            settings: require('./icons/theme/dark/settings.png'),
            cross: require('./icons/theme/dark/cross.png'),
            clip: require('./icons/theme/dark/clip.png'),
            send: require('./icons/theme/dark/paper-plane.png'),
            menu: require('./icons/theme/dark/menu.png'),
            edit: require('./icons/theme/dark/edit.png'),
            options: require('./icons/theme/dark/options.png')
        },
        light: {
            profile: require('./icons/theme/light/profile.png'),
            settings: require('./icons/theme/light/settings.png'),
            cross: require('./icons/theme/light/cross.png'),
            clip: require('./icons/theme/light/clip.png'),
            send: require('./icons/theme/light/paper-plane.png'),
            menu: require('./icons/theme/light/menu.png'),
            edit: require('./icons/theme/light/edit.png'),
            options: require('./icons/theme/light/options.png')
        }
    };


    const openUserProfile = (userOrChat) => {
        const user = userOrChat.username ? {
            username: userOrChat.username,
            firstname: userOrChat.firstname,
            lastname: userOrChat.lastname,
            id: userOrChat.id
        } : userOrChat;

        setViewedUser(user);
        setShowUserProfile(true);
    };

    const handleUserSelect = (user) => {
        openUserProfile(user);
        setSearchQuery('');
        setShowSearchResults(false);
    };

    const createChat = async () => {
        try {
            const response = await makeAuthRequest(() =>
                apiAxios.post('/chats', {
                    participants: [user.id, viewedUser.id]
                })
            );

            setShowOptions(false);
            setShowUserProfile(false);

            loadChats(0, 15, true);

            setSelectedChat(response.data);

        } catch (error) {
            console.error("Error creating chat:", error);
        }
    };

    const [icons, setIcons] = useState(iconPaths.light);

    const searchUsers = async (query) => {
        try {
            const response = await makeAuthRequest(() =>
                apiAxios.get(`/users/username/starts-with/${query}`)
            );
            setSearchResults(response.data.content.slice(0, 5));
            setShowSearchResults(true); // Добавьте эту строку
        } catch (error) {
            console.error("Error searching users:", error);
            setSearchResults([]);
            setShowSearchResults(false);
        }
    };

    const handleSearchChange = (e) => {
        const query = e.target.value;
        setSearchQuery(query);

        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        if (query.length > 0) {
            setSearchTimeout(setTimeout(() => {
                searchUsers(query);
            }, 300));
        } else {
            setSearchResults([]);
            setShowSearchResults(false);
        }
    };

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (e.target.closest('.search-container') === null) {
                setShowSearchResults(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const changeTheme = (themeName) => {
        setCurrentTheme(themeName);
        const theme = themes[themeName];
        Object.keys(theme).forEach(key => {
            document.documentElement.style.setProperty(key, theme[key]);
        });
        setIcons(iconPaths[themeName]);
        localStorage.setItem('theme', themeName);
    };

    useEffect(() => {
        const savedTheme = localStorage.getItem('theme') || 'dark';
        changeTheme(savedTheme);
    }, []);

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (showOptions && e.target.closest('.options-container') === null) {
                setShowOptions(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [showOptions]);

    const [userColors, setUserColors] = useState(() => {
        const savedColors = loadColorsFromStorage();
        return savedColors;
    });

    const openSettingsModal = () => {
        setShowSettingsModal(true);
        setSidebarActive(false);
    };

    const openProfileModal = () => {
        setShowProfileModal(true);
        setSidebarActive(false);
    };

    const closeModal = () => {
        setShowSettingsModal(false);
        setShowProfileModal(false);
    };

    useEffect(() => {
        if (!user) return;

        const token = localStorage.getItem('jwtToken');
        if (!token) {
            logout();
            return;
        }

        const client = new Client({
            brokerURL: WS_URL,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            connectHeaders: {
                Authorization: `Bearer ${token}`
            },
            debug: (str) => {
                console.log('STOMP:', str);
            },
            onConnect: () => {
                console.log('WebSocket Connected');
                setStompClient(client);
            },
            onStompError: (frame) => {
                console.error('Broker reported error:', frame.headers.message);
                console.error('Additional details:', frame.body);
                if (frame.headers.message.includes('401')) {
                    logout();
                }
            }
        });

        client.activate();

        return () => {
            if (client && client.connected) {
                client.deactivate();
            }
        };
    }, [user, logout]);

    useEffect(() => {
        if (!stompClient || !selectedChat?.sharedId || !user) return;

        const subscription = stompClient.subscribe(
            `/topic/chat.${selectedChat.sharedId}`,
            (message) => {
                try {
                    const newMessage = JSON.parse(message.body);
                    if (newMessage.sender !== user.id) {
                        setMessages(prev => [...prev, {
                            id: Date.now().toString(),
                            text: newMessage.content,
                            isSent: false,
                            timestamp: new Date().toISOString()
                        }]);
                    }
                } catch (e) {
                    console.error('Error parsing WebSocket message:', e);
                }
            }
        );

        return () => subscription.unsubscribe();
    }, [stompClient, selectedChat, user]);

    useEffect(() => {
        if (user && !isInitialized) {
            loadChats(0, 15, true);
            setIsInitialized(true);
        }
    }, [user]);

    useEffect(() => {
        if (selectedChat && user) {
            setMessages([]);
            setMessagePage(0);
            setHasMoreMessages(true);
            loadMessages(selectedChat.chatId, 0, 20);
        }
    }, [selectedChat, user]);

    useEffect(() => {
        messagesContainerRef.current?.scrollTo({
            top: messagesContainerRef.current.scrollHeight,
            behavior: 'smooth'
        });
    }, [messages]);

    useEffect(() => {
        const container = messagesContainerRef.current;
        if (!container || !selectedChat) return;

        const handleScroll = debounce(() => {
            if (isLoading || !hasMoreMessages) return;

            const { scrollTop } = container;
            if (scrollTop < 100) { // Более точное определение "верха"
                loadMessages(selectedChat.chatId, messagePage + 1, 20, true);
            }
        }, 200);

        container.addEventListener('scroll', handleScroll);
        return () => container.removeEventListener('scroll', handleScroll);
    }, [selectedChat, isLoading, hasMoreMessages, messagePage]); // Добавлена зависимость messagePage

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (sidebarActive && !e.target.closest('.sidebar') && !e.target.closest('.menu-button')) {
                setSidebarActive(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [sidebarActive]);

    function debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    const loadMessages = async (chatId, page = 0, size = 20, append = false) => {
        try {
            const response = await makeAuthRequest(() =>
                apiAxios.get(`/chats/${chatId}/messages`, {
                    params: { page_number: page, size: size }
                })
            );

            const data = response.data;
            if (!data?.content) throw new Error("Invalid messages format");

            const formattedMessages = data.content.map(msg => ({
                id: msg.id,
                text: msg.content,
                isSent: msg.sender === user.id,
                timestamp: msg.timestamp
            }));

            const sortedMessages = [...formattedMessages].reverse();
            setMessages(prev => append ? [...sortedMessages, ...prev] : sortedMessages);
            setHasMoreMessages(!data.last);
            setMessagePage(page);
        } catch (error) {
            console.error("Error loading messages:", error);
            setError(error.response?.data?.message || "Failed to load messages");
        } finally {
            setIsLoading(false);
        }
    };

    const shouldShowAvatar = (messages, index) => {
        if (index === messages.length - 1) return true;

        const current = messages[index];
        const next = messages[index + 1];
        return current.isSent !== next.isSent;
    };

    const handleContactsScroll = () => {
        if (!contactsListRef.current || isLoading || !hasMore) return;

        const { scrollTop, scrollHeight, clientHeight } = contactsListRef.current;
        const isNearBottom = scrollHeight - (scrollTop + clientHeight) < 50;

        if (isNearBottom && hasMore) {
            loadChats(currentPage + 1, 15);
        }
    };

    const loadChats = async (page = 0, size = 15, isInitialLoad = false) => {
        if (!user) return;
        setIsLoading(true);
        setError(null);

        try {
            const response = await makeAuthRequest(() =>
                apiAxios.get(`/chats/username/${user.username}`, {
                    params: { page_number: page, size: size }
                })
            );

            const data = response.data;
            if (!data?.content) throw new Error("Invalid data format");

            const newContacts = isInitialLoad
                ? data.content
                : [...contacts, ...data.content.filter(chat => !contacts.some(c => c.chatId === chat.chatId))];

            const colors = initUserColors(
                newContacts.map(c => ({ username: c.username })),
                user
            );

            setUserColors(prev => {
                const mergedColors = { ...prev, ...colors };
                saveColorsToStorage(mergedColors);
                return mergedColors;
            });

            setContacts(newContacts);
            setCurrentPage(data.number);
            setHasMore(!data.last);

            if (isInitialLoad && data.content.length > 0 && !selectedChat) {
                setSelectedChat(data.content[0]);
            }
        } catch (error) {
            console.error("Error loading chats:", error);
            if (error.response?.status === 401) logout();
            setError(error.response?.data?.message || error.message);
        } finally {
            setIsLoading(false);
        }
    };

    const sendMessage = async () => {
        const text = messageInput.trim();
        if (!text || !selectedChat || !stompClient || !selectedChat.sharedId || !user) return;

        try {
            const message = {
                content: text,
                sender: user.id,
                sharedId: selectedChat.sharedId,
                timestamp: new Date().toISOString()
            };

            stompClient.publish({
                destination: `/app/chat.send.${selectedChat.sharedId}`,
                body: JSON.stringify(message),
                headers: {
                    'content-type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`
                }
            });

            setMessages(prev => [...prev, {
                id: Date.now().toString(),
                text: text,
                isSent: true,
                timestamp: message.timestamp
            }]);

            setMessageInput('');
        } catch (error) {
            console.error("Error sending message:", error);
            if (error.message.includes('401')) {
                logout();
            }
            setError("Failed to send message");
        } finally {
        }
    };

    const toggleSidebar = () => {
        setSidebarActive(!sidebarActive);
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    if (authLoading) return <div className="auth-loading">Checking authentication...</div>;
    if (!user) return <div className="auth-required">Please login to continue</div>;

    return (
        <div className={`app-container ${sidebarActive ? 'sidebar-active' : ''}`}>
            {showProfileModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title">Мой профиль</h2>
                            <div className="modal-actions">
                                <button className="modal-edit">
                                    <img src={icons.edit} alt="Редактировать" />
                                </button>
                                <div className="options-container">
                                    <button className="modal-options" onClick={() => setShowOptions(!showOptions)}>
                                        <img src={icons.options} alt="Опции" />
                                    </button>
                                    {showOptions && (
                                        <div className="options-dropdown">
                                            <button className="option-item" onClick={logout}>
                                                logout
                                            </button>
                                        </div>
                                    )}
                                </div>
                                <button className="modal-close" onClick={closeModal}>
                                    <img src={icons.cross} alt="Закрыть" />
                                </button>
                            </div>
                        </div>

                        <div className="profile-header">
                            <div className="profile-avatar" style={{ backgroundColor: getUserColor(user?.username, userColors) }}>
                                {user?.username?.charAt(0).toUpperCase()}
                            </div>
                            <div className="profile-name">
                                {user?.firstname || 'Имя не указано'}
                            </div>
                        </div>

                        <div className="profile-divider"></div>

                        <div className="profile-details">
                            <div className="profile-detail-item">
                                <div className="detail-title">Имя</div>
                                <div className="detail-value">{user?.firstname || 'Не указано'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Фамилия</div>
                                <div className="detail-value">{user?.lastname || 'Не указана'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Username</div>
                                <div className="detail-value">{user?.username || 'Не указано'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Email</div>
                                <div className="detail-value">{user?.email || 'Не указан'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Дата рождения</div>
                                <div className="detail-value">{user?.birthDate || 'Не указана'}</div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {showUserProfile && viewedUser && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title">Профиль</h2>
                            <div className="modal-actions">
                                <div className="options-container">
                                    <button className="modal-options" onClick={() => setShowOptions(!showOptions)}>
                                        <img src={icons.options} alt="Опции" />
                                    </button>
                                    {showOptions && (
                                        <div className="options-dropdown">
                                            <button className="option-item" onClick={createChat}>
                                                Создать чат
                                            </button>
                                        </div>
                                    )}
                                </div>
                                <button className="modal-close" onClick={() => setShowUserProfile(false)}>
                                    <img src={icons.cross} alt="Закрыть" />
                                </button>
                            </div>
                        </div>

                        <div className="profile-header">
                            <div className="profile-avatar" style={{ backgroundColor: getUserColor(viewedUser.username, userColors) }}>
                                {viewedUser.username?.charAt(0).toUpperCase()}
                            </div>
                            <div className="profile-name">{viewedUser.firstname || viewedUser.username}</div>
                        </div>

                        <div className="profile-divider"></div>

                        <div className="profile-details">
                            <div className="profile-detail-item">
                                <div className="detail-title">Имя</div>
                                <div className="detail-value">{viewedUser.firstname || 'Не указано'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Фамилия</div>
                                <div className="detail-value">{viewedUser.lastname || 'Не указана'}</div>
                            </div>

                            <div className="profile-detail-item">
                                <div className="detail-title">Username</div>
                                <div className="detail-value">{viewedUser.username}</div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {showSettingsModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h2 className="modal-title">Настройки</h2>
                            <button className="modal-close" onClick={closeModal}>
                                <img src={icons.cross} alt="Закрыть" />
                            </button>
                        </div>
                        <div className="modal-body">
                            <div className="settings-row">
                                <label className="settings-label">Тема:</label>
                                <select
                                    className="settings-select"
                                    value={currentTheme}
                                    onChange={(e) => changeTheme(e.target.value)}
                                >
                                    <option value="dark">Темная</option>
                                    <option value="light">Светлая</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>
            )}
            <div className="left-stripe">
                <button className="menu-button" onClick={toggleSidebar}>
                    <img src={icons.menu} alt="Меню" className="menu-icon"/>
                </button>
            </div>

            <div className={`sidebar ${sidebarActive ? 'active' : ''}`}>
                <div className="sidebar-item" onClick={openProfileModal}>
                    <img src={icons.profile} alt="Профиль" className="sidebar-icon"/>
                    <span className="sidebar-text">Профиль</span>
                </div>
                <div className="sidebar-item" onClick={openSettingsModal}>
                    <img src={icons.settings} alt="Настройки" className="sidebar-icon"/>
                    <span className="sidebar-text">Настройки</span>
                </div>
            </div>

            <div className="contacts-panel">
                <div className="search-container">
                    <div className="search">
                        <input
                            type="text"
                            className="search-input"
                            value={searchQuery}
                            onChange={handleSearchChange}
                            placeholder="Поиск..."
                            onFocus={() => searchQuery.length > 0 && setShowSearchResults(true)}
                        />
                    </div>

                    {showSearchResults && (
                        <div className="search-results">
                            {searchResults.length > 0 ? (
                                searchResults.map((user) => (
                                    <div
                                        key={user.id}
                                        className="search-result-item"
                                        onClick={() => handleUserSelect(user)}
                                    >
                                        <div
                                            className="contact-avatar"
                                            style={{ backgroundColor: getUserColor(user.username, userColors) }}
                                        >
                                            {user.username?.charAt(0).toUpperCase()}
                                        </div>
                                        <span className="contact-name">{user.username}</span>
                                    </div>
                                ))
                            ) : (
                                <div className="no-results">Пользователи не найдены</div>
                            )}
                        </div>
                    )}
                </div>
                <div
                    className="contacts-list"
                    ref={contactsListRef}
                    onScroll={handleContactsScroll}
                >
                    {isLoading && contacts.length === 0 ? (
                        <div className="loading">Загрузка...</div>
                    ) : (
                        contacts.map((contact, index) => (
                            <div
                                key={index}
                                className={`contact ${selectedChat?.chatId === contact.chatId ? 'active' : ''}`}
                                onClick={() => setSelectedChat(contact)}
                            >
                                <div
                                    className="contact-avatar"
                                    style={{ backgroundColor: getUserColor(contact.username, userColors) }}
                                >
                                    {contact.username?.charAt(0).toUpperCase()}
                                </div>
                                <span className="contact-name">{contact.username}</span>
                            </div>
                        ))
                    )}
                </div>
            </div>

            <div className="chat-area">
                {error && <div className="error-message">{error}</div>}

                {selectedChat ? (
                    <>
                        <div className="chat-header" onClick={() => openUserProfile(selectedChat)}>
                            <div className="current-chat-user">
                                <span className="header-name">{selectedChat.username}</span>
                            </div>
                        </div>

                        <div className="messages" ref={messagesContainerRef}>
                            {isLoading && messages.length === 0 ? (
                                <div className="loading">Загрузка сообщений...</div>
                            ) : (
                                messages.map((message, index) => {
                                    const showAvatar = shouldShowAvatar(messages, index);
                                    const username = message.isSent ? user.username : selectedChat?.username;
                                    const avatarChar = username ? username.charAt(0).toUpperCase() : '';

                                    return (
                                        <div
                                            key={message.id}
                                            className={`message ${message.isSent ? 'sent' : 'received'} ${showAvatar ? 'show-avatar' : ''}`}
                                        >
                                            <div
                                                className="message-avatar"
                                                style={{
                                                    backgroundColor: message.isSent
                                                        ? getUserColor(user.username, userColors)
                                                        : getUserColor(selectedChat?.username, userColors)
                                                }}
                                            >
                                                {avatarChar}
                                            </div>
                                            <div className="message-content">{message.text}</div>
                                        </div>
                                    );
                                })
                            )}
                            <div ref={messagesEndRef} />
                        </div>

                        <div className="message-input">
                            <button className="attach-button">
                                <img src={icons.clip} alt="Прикрепить"/>
                            </button>
                            <input
                                type="text"
                                className="input-field"
                                value={messageInput}
                                onChange={(e) => setMessageInput(e.target.value)}
                                onKeyPress={handleKeyPress}
                                placeholder="Написать сообщение..."
                            />
                            <button className="send-button" onClick={sendMessage}>
                                <img src={icons.send} alt="Отправить"/>
                            </button>
                        </div>
                    </>
                ) : (
                    <div className="empty-chat">
                        <p>Выберите чат для начала общения</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Home;