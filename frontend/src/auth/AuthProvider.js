import React, { createContext, useEffect, useState } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8000';
    const API_VERSION = process.env.REACT_APP_API_VERSION || 'v1';
    const API_URL = `${API_BASE_URL}/api/${API_VERSION}`;
    const authAxios = axios.create({
        baseURL: API_URL
    });

    const apiAxios = axios.create({
        baseURL: API_URL
    });

    apiAxios.interceptors.request.use(config => {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    });

    const refreshTokens = async () => {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            if (!refreshToken) throw new Error('No refresh token');

            const response = await authAxios.post('/users/refresh-token', {
                refreshToken
            });

            localStorage.setItem('jwtToken', response.data.access_token);
            localStorage.setItem('refreshToken', response.data.refresh_token);
            return response.data.access_token;
        } catch (error) {
            console.error('Token refresh failed:', error);
            logout();
            throw error;
        }
    };

    const makeAuthRequest = async (requestFn) => {
        try {
            return await requestFn();
        } catch (error) {
            if (error.response?.status === 401 && !error.config._retry) {
                try {
                    const newToken = await refreshTokens();
                    error.config.headers.Authorization = `Bearer ${newToken}`;
                    error.config._retry = true;
                    return apiAxios(error.config);
                } catch (refreshError) {
                    logout();
                    throw refreshError;
                }
            }
            throw error;
        }
    };

    const checkAuth = async () => {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            setLoading(false);
            return;
        }

        try {
            const { data } = await makeAuthRequest(() =>
                apiAxios.get('/users/me')
            );
            setUser(data);
        } catch (error) {
            logout();
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        checkAuth();

        const interceptor = apiAxios.interceptors.response.use(
            response => response,
            async error => {
                if (error.response?.status === 401 && !error.config._retry) {
                    try {
                        const newToken = await refreshTokens();
                        error.config.headers.Authorization = `Bearer ${newToken}`;
                        error.config._retry = true;
                        return apiAxios(error.config);
                    } catch (refreshError) {
                        logout();
                        return Promise.reject(refreshError);
                    }
                }
                return Promise.reject(error);
            }
        );

        return () => apiAxios.interceptors.response.eject(interceptor);
    }, []);

    const login = async (username, password) => {
        try {
            const response = await authAxios.post('/users/sign-in', {
                username,
                password
            });

            localStorage.setItem('jwtToken', response.data.access_token);
            localStorage.setItem('refreshToken', response.data.refresh_token);

            const userResponse = await apiAxios.get('/users/me');
            setUser(userResponse.data);
            return true;
        } catch (error) {
            console.error('Login failed:', error);
            return false;
        }
    };

    const logout = () => {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('refreshToken');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{
            user,
            loading,
            login,
            logout,
            checkAuth,
            apiAxios,
            makeAuthRequest,
            apiBaseUrl: API_BASE_URL
        }}>
            {children}
        </AuthContext.Provider>
    );
};