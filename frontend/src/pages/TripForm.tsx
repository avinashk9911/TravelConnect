import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { tripsApi } from '../api/bookings';

export function TripForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    travelerId: searchParams.get('travelerId') ?? '',
    name: '',
    destination: '',
    startDate: '',
    endDate: '',
    description: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const trip = await tripsApi.create(form);
      navigate(`/trips/${trip.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create trip');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">New Trip</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card" style={{ maxWidth: '600px' }}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Traveler ID *</label>
            <input className="form-input" name="travelerId" value={form.travelerId} onChange={handleChange} placeholder="UUID of the traveler" required />
          </div>
          <div className="form-group">
            <label className="form-label">Trip Name *</label>
            <input className="form-input" name="name" value={form.name} onChange={handleChange} placeholder="e.g. Q1 Sales Conference" required />
          </div>
          <div className="form-group">
            <label className="form-label">Destination *</label>
            <input className="form-input" name="destination" value={form.destination} onChange={handleChange} placeholder="e.g. New York, USA" required />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Start Date *</label>
              <input className="form-input" type="date" name="startDate" value={form.startDate} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">End Date *</label>
              <input className="form-input" type="date" name="endDate" value={form.endDate} onChange={handleChange} required />
            </div>
          </div>
          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Trip'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate(-1)}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
