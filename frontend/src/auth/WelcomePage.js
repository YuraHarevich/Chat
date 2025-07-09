import React from 'react';
import '../styles/Welcome.css';
import welcomeImage from '../icons/backgrounds/riot_of_colors_1.png';
import {useNavigate} from "react-router-dom"; // Импортируем изображение

const Welcome = () => {
    const navigate = useNavigate();

    return (
        <div className="welcome-container">
            <div className="welcome-image">
                <img
                    src={welcomeImage}
                    alt="Chat Messenger"
                />
            </div>
            <div className="welcome-content">
                <h1 className="welcome-title">
                    JOIN<br />
                    CHAT MESSENGER
                </h1>

                <div className="welcome-buttons">
                    <button
                        className="welcome-btn outline"
                        onClick={() => navigate('/registration')}>
                        Sign Up
                    </button>
                    <button className="welcome-btn outline">
                        Sign up with Google
                    </button>
                </div>

                <div className="welcome-footer">
                    <p className="welcome-text">Already have an account?</p>
                    <button
                        className="welcome-btn filled"
                        onClick={() => navigate('/login')}>
                        Sign In
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Welcome;