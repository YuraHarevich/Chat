// src/App.js
import React, {Component} from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from "./auth/Login";
import PrivateRoute from "./auth/PrivateRoute";
import Home from "./Home";
import Welcome from "./auth/WelcomePage";
import Registration from "./auth/Registration";

class App extends Component {
    render() {
        return (
            <Router>
                <Routes>
                    <Route path="/" element={<Navigate to="/home" replace />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/welcome" element={<Welcome />} />
                    <Route path="/registration" element={<Registration />} />
                    <Route path="/home" element={<PrivateRoute><Home /></PrivateRoute>} />
                </Routes>
            </Router>
        );
    }
}

export default App;
