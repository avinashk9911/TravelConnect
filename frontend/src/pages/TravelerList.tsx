import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { travelersApi } from '../api/travelers';
import type { Traveler, PagedResponse } from '../types';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function TravelerList() {
  const [data, setData] = useState<PagedResponse<Traveler> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    travelersApi.getAll(0, 20)
      .then(setData)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>Travelers</h1>
        <Link to="/travelers/new" className="btn btn-primary">+ New Traveler</Link>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        {data && data.content.length > 0 ? (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Nationality</th>
                <th>Passport Expiry</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map(t => (
                <tr key={t.id}>
                  <td>{t.firstName} {t.lastName}</td>
                  <td>{t.email}</td>
                  <td>{t.nationality ?? '—'}</td>
                  <td>{t.passportExpiry ?? '—'}</td>
                  <td>
                    <Link to={`/travelers/${t.id}`} className="btn btn-secondary" style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}>View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p style={{ color: '#718096', textAlign: 'center', padding: '2rem' }}>
            No travelers found. <Link to="/travelers/new">Create the first one</Link>.
          </p>
        )}
      </div>
    </div>
  );
}
