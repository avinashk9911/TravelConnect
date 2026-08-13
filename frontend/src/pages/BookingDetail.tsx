import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { bookingsApi } from '../api/bookings';
import { integrationsApi } from '../api/integrations';
import type { Booking, IntegrationRequest } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';

export function BookingDetail() {
  const { id } = useParams<{ id: string }>();
  const [booking, setBooking] = useState<Booking | null>(null);
  const [integrations, setIntegrations] = useState<IntegrationRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      bookingsApi.getById(id),
      integrationsApi.getByBooking(id).catch(() => [] as IntegrationRequest[]),
    ]).then(([b, ints]) => {
      setBooking(b);
      setIntegrations(ints);
    })
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!booking) return null;

  return (
    <div className="page">
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem' }}>
        <Link to="/bookings" className="btn btn-secondary">&#8592; Back</Link>
        <h1 className="page-title" style={{ marginBottom: 0 }}>Booking {booking.bookingReference}</h1>
        <StatusBadge status={booking.status} />
      </div>

      <div className="grid-2">
        <div className="card">
          <div className="card-title">Booking Details</div>
          <table className="table">
            <tbody>
              <tr><td><strong>Reference</strong></td><td style={{ fontFamily: 'monospace' }}>{booking.bookingReference}</td></tr>
              <tr><td><strong>Status</strong></td><td><StatusBadge status={booking.status} /></td></tr>
              <tr><td><strong>Total</strong></td><td>{booking.currency} {booking.totalAmount ?? '—'}</td></tr>
              <tr><td><strong>Trace ID</strong></td><td style={{ fontSize: '0.75rem', fontFamily: 'monospace' }}>{booking.traceId ?? '—'}</td></tr>
              <tr><td><strong>Created</strong></td><td>{new Date(booking.createdAt).toLocaleDateString()}</td></tr>
            </tbody>
          </table>
        </div>

        <div className="card">
          <div className="card-title">Booking Items ({booking.items?.length ?? 0})</div>
          {booking.items?.map(item => (
            <div key={item.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid #e2e8f0' }}>
              <strong>{item.itemType}</strong>
              {item.origin && item.destination && (
                <span style={{ fontSize: '0.875rem', color: '#718096' }}> &#8212; {item.origin} &#8594; {item.destination}</span>
              )}
              {item.pricePerUnit && (
                <span style={{ float: 'right', color: '#38a169' }}>{item.currency} {item.pricePerUnit}</span>
              )}
            </div>
          ))}
        </div>
      </div>

      {integrations.length > 0 && (
        <div className="card" style={{ marginTop: '1rem' }}>
          <div className="card-title">Supplier Integrations</div>
          <table className="table">
            <thead>
              <tr>
                <th>Supplier</th>
                <th>Type</th>
                <th>Status</th>
                <th>Retries</th>
                <th>Trace ID</th>
              </tr>
            </thead>
            <tbody>
              {integrations.map(i => (
                <tr key={i.id}>
                  <td>{i.supplierId}</td>
                  <td>{i.supplierType}</td>
                  <td><StatusBadge status={i.status} /></td>
                  <td>{i.retryCount}</td>
                  <td style={{ fontSize: '0.75rem', fontFamily: 'monospace' }}>{i.traceId}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
