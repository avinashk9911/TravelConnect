import { useState } from 'react';
import axios from 'axios';

interface ActuatorHealth {
  status: string;
  components?: Record<string, { status: string }>;
}

export function AdminMonitoring() {
  const [healthData, setHealthData] = useState<Record<string, ActuatorHealth | null>>({});
  const [perfResult, setPerfResult] = useState<string | null>(null);
  const [loading, setLoading] = useState<string | null>(null);

  const services = [
    { name: 'Traveler Service', url: 'http://localhost:8081/actuator/health', port: 8081 },
    { name: 'Booking Service', url: 'http://localhost:8082/actuator/health', port: 8082 },
    { name: 'Integration Service', url: 'http://localhost:8083/actuator/health', port: 8083 },
    { name: 'Notification Service', url: 'http://localhost:8084/actuator/health', port: 8084 },
  ];

  const checkHealth = async (name: string, url: string) => {
    setLoading(name);
    try {
      const res = await axios.get<ActuatorHealth>(url, { timeout: 3000 });
      setHealthData(prev => ({ ...prev, [name]: res.data }));
    } catch {
      setHealthData(prev => ({ ...prev, [name]: null }));
    } finally {
      setLoading(null);
    }
  };

  const runPerfTest = async () => {
    setLoading('perf');
    try {
      const res = await axios.post(
        'http://localhost:8084/api/v1/admin/notifications/perf-test?iterations=1000000',
        {},
        { timeout: 30000 },
      );
      setPerfResult((res.data as { data?: string }).data ?? 'Done');
    } catch {
      setPerfResult('Failed — is the notification service running?');
    } finally {
      setLoading(null);
    }
  };

  const actuatorEndpoints: [string, string][] = [
    [':808x/actuator/health', 'Service health (DB, RabbitMQ status)'],
    [':808x/actuator/metrics', 'JVM metrics, HTTP request counts'],
    [':808x/actuator/metrics/jvm.memory.used', 'Current heap usage'],
    [':808x/actuator/metrics/http.server.requests', 'Request latencies by endpoint'],
    [':808x/actuator/threaddump', 'Thread dump — investigate blocked threads'],
    [':808x/actuator/heapdump', 'Heap dump file — analyse with VisualVM/Eclipse MAT'],
    [':808x/actuator/env', 'Active configuration values'],
  ];

  return (
    <div className="page">
      <h1 className="page-title">Admin Monitoring</h1>

      <div className="card">
        <div className="card-title">Service Health (Actuator)</div>
        <table className="table">
          <thead>
            <tr><th>Service</th><th>Status</th><th>Action</th></tr>
          </thead>
          <tbody>
            {services.map(s => (
              <tr key={s.name}>
                <td>{s.name} (:{s.port})</td>
                <td>
                  {healthData[s.name] === undefined && <span style={{ color: '#718096' }}>&#8212;</span>}
                  {healthData[s.name] === null && <span className="badge badge-failed">UNREACHABLE</span>}
                  {healthData[s.name] != null && (
                    <span className={`badge ${healthData[s.name]!.status === 'UP' ? 'badge-confirmed' : 'badge-failed'}`}>
                      {healthData[s.name]!.status}
                    </span>
                  )}
                </td>
                <td>
                  <button
                    className="btn btn-secondary"
                    style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                    disabled={loading === s.name}
                    onClick={() => checkHealth(s.name, s.url)}
                  >
                    {loading === s.name ? 'Checking...' : 'Check'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <p style={{ fontSize: '0.75rem', color: '#718096', marginTop: '0.5rem' }}>
          Note: Direct cross-origin requests to other ports may fail in browser due to CORS. Use curl to check health in development.
        </p>
      </div>

      <div className="card">
        <div className="card-title">Performance Test (Educational Demo)</div>
        <p style={{ fontSize: '0.875rem', color: '#4a5568', marginBottom: '1rem' }}>
          Triggers a bounded CPU workload on the Notification Service to demonstrate performance investigation
          with JVM tools (thread dumps, heap dumps, Actuator metrics).
        </p>
        <button className="btn btn-danger" disabled={loading === 'perf'} onClick={runPerfTest}>
          {loading === 'perf' ? 'Running...' : 'Run Performance Test'}
        </button>
        {perfResult && <div className="alert alert-info" style={{ marginTop: '1rem' }}>{perfResult}</div>}
      </div>

      <div className="card">
        <div className="card-title">Useful Actuator Endpoints</div>
        <table className="table">
          <thead>
            <tr><th>Endpoint</th><th>Purpose</th></tr>
          </thead>
          <tbody>
            {actuatorEndpoints.map(([ep, desc], i) => (
              <tr key={i}>
                <td><code style={{ fontSize: '0.8rem' }}>{ep}</code></td>
                <td style={{ fontSize: '0.875rem' }}>{desc}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
