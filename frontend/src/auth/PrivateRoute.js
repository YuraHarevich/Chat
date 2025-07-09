import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from './AuthProvider';

const PrivateRoute = ({ children }) => {
    const { user, loading } = useContext(AuthContext);
    const hasTokens = localStorage.getItem('jwtToken') || localStorage.getItem('refreshToken');

    if (loading) return <div>Загрузка...</div>;

    if (!user) {
        if (hasTokens) {
            return <Navigate to="/login" replace />;
        } else {
            return <Navigate to="/welcome" replace />;
        }
    }

    return children;
};

export default PrivateRoute;