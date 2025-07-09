import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/Welcome.css';

const Registration = () => {
    const [formData, setFormData] = useState({
        username: '',
        firstname: '',
        lastname: '',
        email: '',
        password: '',
        birthDate: ''
    });
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const SIGN_UP_URL = process.env.REACT_APP_SIGN_UP_URL || '/api/v1/users/sign-up';
    const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8000';

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(
                API_BASE_URL + SIGN_UP_URL,
                {
                    username: formData.username,
                    firstname: formData.firstname,
                    lastname: formData.lastname,
                    email: formData.email,
                    password: formData.password,
                    birthDate: formData.birthDate ? new Date(formData.birthDate).toISOString() : null
                }
            );

            if (response.data) {
                navigate('/login');
            }
        } catch (err) {
            setError(err.response?.data?.message || 'Registration failed');
        }
    };

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 82 }, (_, i) => currentYear - 18 - i);
    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    const days = Array.from({ length: 31 }, (_, i) => i + 1);

    return (
        <div className="signin-container">
            <div className="signin-background"></div>

            <div className="signin-card signin-card-adjusted">
                <h1 className="signin-title">Sign up</h1>
                {error && <div className="error-message">{error}</div>}

                <form className="signin-form" onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input
                            type="text"
                            name="username"
                            className="signin-input"
                            placeholder="Username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            autoComplete="off"
                        />
                    </div>

                    <div className="input-group">
                        <input
                            type="text"
                            name="firstname"
                            className="signin-input"
                            placeholder="First Name"
                            value={formData.firstname}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <input
                            type="text"
                            name="lastname"
                            className="signin-input"
                            placeholder="Last Name"
                            value={formData.lastname}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <input
                            type="email"
                            name="email"
                            className="signin-input"
                            placeholder="Email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <input
                            type="password"
                            name="password"
                            className="signin-input"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <div className="birthdate-selectors">
                            <select
                                name="birthDay"
                                className="signin-input"
                                onChange={(e) => setFormData({...formData, birthDate: `${formData.birthDate.split('-')[0]}-${formData.birthDate.split('-')[1]}-${e.target.value}`})}
                            >
                                <option value="">Day</option>
                                {days.map(day => (
                                    <option key={day} value={day}>{day}</option>
                                ))}
                            </select>

                            <select
                                name="birthMonth"
                                className="signin-input"
                                onChange={(e) => setFormData({...formData, birthDate: `${formData.birthDate.split('-')[0]}-${e.target.value}-${formData.birthDate.split('-')[2]}`})}
                            >
                                <option value="">Month</option>
                                {months.map(month => (
                                    <option key={month} value={month}>{month}</option>
                                ))}
                            </select>

                            <select
                                name="birthYear"
                                className="signin-input"
                                onChange={(e) => setFormData({...formData, birthDate: `${e.target.value}-${formData.birthDate.split('-')[1]}-${formData.birthDate.split('-')[2]}`})}
                            >
                                <option value="">Year</option>
                                {years.map(year => (
                                    <option key={year} value={year}>{year}</option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <button type="submit" className="welcome-btn filled">
                        Sign up
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Registration;