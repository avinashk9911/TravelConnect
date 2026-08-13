import { useEffect, useState } from 'react';
import { integrationsApi } from '../api/integrations';
import { LoadingSpinner } from '../components/LoadingSpinner';

interface SupplierStatus {
  supplierId: string;
  supplierType: string;
  available: boolean;
}

export function IntegrationStatus() {
  const [suppliers, setSuppliers] = useState<SupplierStatus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    integrationsApi.getSupplierStatus()
      .then(setSuppliers)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="page">
      <h1 className="page-title">Integration Status</h1>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="card">
        <div className="card-title">Registered Suppliers</div>
        <table className="table">
          <thead>
            <tr><th>Supplier ID</th><th>Type</th><th>Status</th></tr>
          </thead>
          <tbody>
            {suppliers.map(s => (
              <tr key={s.supplierId}>
                <td style={{ fontFamily: 'monospace' }}>{s.supplierId}</td>
                <td>{s.supplierType}</td>
                <td>
                  <span className={`badge ${s.available ? 'badge-confirmed' : 'badge-failed'}`}>
                    {s.available ? 'AVAILABLE' : 'UNAVAILABLE'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <div className="card-title">Integration Architecture</div>
        <pre style={{
          fontSize: '0.8rem',
          background: '#f7fafc',
          padding: '1rem',
          borderRadius: '4px',
          overflowX: 'auto',
          lineHeight: '1.6',
        }}>{`
Booking Service
     |
     | publishes BookingCreated event
     v
  RabbitMQ
     |
     | booking.created routing key
     v
Integration Service
     |
     +──> FlightSupplierAdapter (REST/JSON) ──> Flight Supplier :9001
     +──> HotelSupplierAdapter  (REST/JSON) ──> Hotel Supplier  :9002
     +──> CarSupplierAdapter    (SOAP/XML)  ──> Car Supplier    :9003
     |
     | publishes SupplierResponseReceived events
     v
  RabbitMQ
     |
     v
Booking Service (updates booking status)
`}</pre>
      </div>
    </div>
  );
}
