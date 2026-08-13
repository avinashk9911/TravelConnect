import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { travelersApi } from '../api/travelers';
import type { Traveler } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function TravelerDetail() {
  const { id } = useParams<{ id: string }>();
  const [traveler, setTraveler] = useState<Traveler | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    travelersApi.getById(id)
      .then(setTraveler)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!traveler) return null;

  return (
    <div className="page">
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
        <Link to="/travelers" className="btn btn-secondary">&#8592; Back</Link>
        <h1 className="page-title" style={{ marginBottom: 0 }}>
          {traveler.firstName} {traveler.lastName}
        </h1>
      </div>

      <div className="grid-2">
        <div className="card">
          <div className="card-title">Contact Information</div>
          <table className="table">
            <tbody>
              <tr><td><strong>Email</strong></td><td>{traveler.email}</td></tr>
              <tr><td><strong>Phone</strong></td><td>{traveler.phone ?? '—'}</td></tr>
              <tr><td><strong>Date of Birth</strong></td><td>{traveler.dateOfBirth ?? '—'}</td></tr>
              <tr><td><strong>Nationality</strong></td><td>{traveler.nationality ?? '—'}</td></tr>
            </tbody>
          </table>
        </div>
        <div className="card">
          <div className="card-title">Travel Documents</div>
          <table className="table">
            <tbody>
              <tr><td><strong>Passport No.</strong></td><td>{traveler.passportNumber ?? '—'}</td></tr>
              <tr><td><strong>Expiry Date</strong></td><td>{traveler.passportExpiry ?? '—'}</td></tr>
              <tr><td><strong>Created</strong></td><td>{new Date(traveler.createdAt).toLocaleDateString()}</td></tr>
              <tr><td><strong>Traveler ID</strong></td><td style={{ fontSize: '0.75rem', fontFamily: 'monospace' }}>{traveler.id}</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div style={{ marginTop: '1rem' }}>
        <Link to={`/trips/new?travelerId=${traveler.id}`} className="btn btn-primary" style={{ marginRight: '0.5rem' }}>
          + Create Trip for this Traveler
        </Link>
        <Link to={`/bookings?travelerId=${traveler.id}`} className="btn btn-secondary">
          View Bookings
        </Link>
      </div>
    </div>
  );
}
