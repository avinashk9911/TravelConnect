import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { travelersApi, type CreateTravelerData } from '../api/travelers';

export function TravelerForm() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState<CreateTravelerData>({
    firstName: '', lastName: '', email: '', phone: '',
    nationality: '', passportNumber: '', passportExpiry: '', dateOfBirth: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const traveler = await travelersApi.create(form);
      navigate(`/travelers/${traveler.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create traveler');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">New Traveler</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card" style={{ maxWidth: '600px' }}>
        <form onSubmit={handleSubmit}>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">First Name *</label>
              <input className="form-input" name="firstName" value={form.firstName} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name *</label>
              <input className="form-input" name="lastName" value={form.lastName} onChange={handleChange} required />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Email *</label>
            <input className="form-input" type="email" name="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="form-input" name="phone" placeholder="+441234567890" value={form.phone} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Date of Birth</label>
              <input className="form-input" type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handleChange} />
            </div>
          </div>
          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Nationality</label>
              <input className="form-input" name="nationality" value={form.nationality} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Passport Number</label>
              <input className="form-input" name="passportNumber" value={form.passportNumber} onChange={handleChange} />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Passport Expiry</label>
            <input className="form-input" type="date" name="passportExpiry" value={form.passportExpiry} onChange={handleChange} />
          </div>
          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Creating...' : 'Create Traveler'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => navigate('/travelers')}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
}
