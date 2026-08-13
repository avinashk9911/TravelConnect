import { Link } from 'react-router-dom';

export function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">&#9992; TravelConnect</Link>
      <div className="navbar-links">
        <Link to="/">Dashboard</Link>
        <Link to="/travelers">Travelers</Link>
        <Link to="/search">Search</Link>
        <Link to="/trips">Trips</Link>
        <Link to="/bookings">Bookings</Link>
        <Link to="/integrations">Integration</Link>
        <Link to="/admin">Admin</Link>
      </div>
    </nav>
  );
}
