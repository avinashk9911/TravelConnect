import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { Dashboard } from './pages/Dashboard';
import { TravelerList } from './pages/TravelerList';
import { TravelerForm } from './pages/TravelerForm';
import { TravelerDetail } from './pages/TravelerDetail';
import { SearchTravel } from './pages/SearchTravel';
import { TripForm } from './pages/TripForm';
import { BookingList } from './pages/BookingList';
import { BookingDetail } from './pages/BookingDetail';
import { IntegrationStatus } from './pages/IntegrationStatus';
import { AdminMonitoring } from './pages/AdminMonitoring';

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/travelers" element={<TravelerList />} />
        <Route path="/travelers/new" element={<TravelerForm />} />
        <Route path="/travelers/:id" element={<TravelerDetail />} />
        <Route path="/search" element={<SearchTravel />} />
        <Route path="/trips/new" element={<TripForm />} />
        <Route path="/bookings" element={<BookingList />} />
        <Route path="/bookings/:id" element={<BookingDetail />} />
        <Route path="/integrations" element={<IntegrationStatus />} />
        <Route path="/admin" element={<AdminMonitoring />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
