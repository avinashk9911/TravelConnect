import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import type { Booking, PagedResponse } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function BookingList() {
  const [searchParams] = useSearchParams();
  const travelerId = searchParams.get('travelerId');
  const [data, setData] = useState<PagedResponse<Booking> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!travelerId) { setLoading(false); return; }
    bookingsApi.getByTraveler(travelerId)
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [travelerId]);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="page">
      <h1 className="page-title">Bookings</h1>
      {!travelerId && (
        <div className="alert alert-info">
          Select a traveler to view their bookings, or navigate from a traveler&apos;s profile.
        </div>
      )}
      {error && <div className="alert alert-error">{error}</div>}
      {data && (
        <div className="card">
          <table className="table">
            <thead>
              <tr>
                <th>Reference</th>
                <th>Status</th>
                <th>Amount</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(b => (
                <tr key={b.id}>
                  <td style={{ fontFamily: 'monospace', fontWeight: 600 }}>{b.bookingReference}</td>
                  <td><StatusBadge status={b.status} /></td>
                  <td>{b.currency} {b.totalAmount ?? '—'}</td>
                  <td>{new Date(b.createdAt).toLocaleDateString()}</td>
                  <td>
                    <Link to={`/bookings/${b.id}`} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}>View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
