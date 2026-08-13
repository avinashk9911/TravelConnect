import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { travelersApi } from '../api/travelers';
import { integrationsApi } from '../api/integrations';

export function Dashboard() {
  const [stats, setStats] = useState({ travelers: 0, suppliers: 0 });

  useEffect(() => {
    Promise.allSettled([
      travelersApi.getAll(0, 1),
      integrationsApi.getSupplierStatus(),
    ]).then(([travelers, suppliers]) => {
      setStats({
        travelers: travelers.status === 'fulfilled' ? travelers.value.totalElements : 0,
        suppliers: suppliers.status === 'fulfilled' ? suppliers.value.length : 0,
      });
    });
  }, []);

  return (
    <div className="page">
      <h1 className="page-title">Dashboard</h1>

      <div className="grid-3">
        <div className="card stat-card">
          <div className="stat-number">{stats.travelers}</div>
          <div className="stat-label">Travelers</div>
        </div>
        <div className="card stat-card">
          <div className="stat-number">{stats.suppliers}</div>
          <div className="stat-label">Active Suppliers</div>
        </div>
        <div className="card stat-card">
          <div className="stat-number">&#10003;</div>
          <div className="stat-label">System Status</div>
        </div>
      </div>

      <div className="grid-2" style={{ marginTop: '1.5rem' }}>
        <div className="card">
          <div className="card-title">Quick Actions</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <Link to="/travelers/new" className="btn btn-primary">+ Create Traveler</Link>
            <Link to="/search" className="btn btn-secondary">Search Travel</Link>
            <Link to="/trips/new" className="btn btn-secondary">Create Trip</Link>
          </div>
        </div>

        <div className="card">
          <div className="card-title">Business Flow</div>
          <ol style={{ paddingLeft: '1.25rem', fontSize: '0.875rem', lineHeight: '2' }}>
            <li>Create traveler profile</li>
            <li>Search for flights/hotels/cars</li>
            <li>Create a trip</li>
            <li>Create a booking</li>
            <li>System sends to suppliers via RabbitMQ</li>
            <li>Booking confirmed</li>
            <li>Audit record saved to DynamoDB</li>
          </ol>
        </div>
      </div>
    </div>
  );
}
