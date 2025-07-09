import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from "./AuthProvider";
import axios from 'axios';
import '../styles/Welcome.css';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const SIGN_IN_URL = process.env.REACT_APP_SIGN_IN_URL || '/api/v1/users/sign-in';
    const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8000';

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log(API_BASE_URL + SIGN_IN_URL)
        try {
            const response = await axios.post(
                API_BASE_URL + SIGN_IN_URL,
                { username, password }
            );

            const token = response.data.access_token;
            if (token) {
                localStorage.setItem('jwtToken', token);
                axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
                await login(username, password);
                navigate('/home');
            } else {
                setError('Не получилось получить токен');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Неверный логин или пароль');
        }
    };

    return (
        <div className="signin-container">
            <div className="signin-background"></div>
            <div className="signin-card signin-card-adjusted">
                <h1 className="signin-title">Sign In</h1>
                {error && <div className="error-message">{error}</div>}

                <form className="signin-form" onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input
                            type="text"
                            id="login"
                            className="signin-input"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                            autoComplete="off"
                        />
                    </div>

                    <div className="input-group">
                        <input
                            type="password"
                            id="password"
                            className="signin-input"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                            autoComplete="current-password"
                        />
                    </div>

                    <button type="submit" className="welcome-btn filled">
                        Sign In
                    </button>
                </form>

                <p className="forgot-password" onClick={() => navigate('/forgot-password')}>
                    Forget your password?
                </p>
            </div>
        </div>
    );
};

export default Login;