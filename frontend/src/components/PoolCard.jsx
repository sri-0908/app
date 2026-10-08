import React from 'react';

export default function PoolCard({ pool, isActive }) {
  // Determine risk level badge based on pool details
  const getRiskBadge = (id) => {
    switch (id) {
      case 'babylon-btc':
        return <span className="badge badge-success">Low Risk (1.0)</span>;
      case 'lorenzo-btc':
        return <span className="badge badge-info" style={{ color: '#f59e0b', background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.3)' }}>Medium Risk (2.5)</span>;
      case 'pell-btc':
        return <span className="badge" style={{ color: '#ef4444', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)' }}>Higher Risk (4.5)</span>;
      default:
        return <span className="badge badge-info">Standard Risk</span>;
    }
  };

  return (
    <div 
      className={`glass-panel ${isActive ? 'pulse-glow' : ''}`}
      style={{
        borderLeft: isActive ? '4px solid var(--accent-btc)' : '1px solid var(--glass-border)',
        position: 'relative',
        overflow: 'hidden'
      }}
    >
      {isActive && (
        <div style={{
          position: 'absolute',
          top: 0,
          right: 0,
          background: 'var(--accent-btc)',
          color: '#000',
          padding: '0.25rem 0.75rem',
          fontSize: '0.75rem',
          fontWeight: 700,
          borderBottomLeftRadius: '8px'
        }}>
          ACTIVE ALLOCATION
        </div>
      )}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
        <div>
          <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.2rem' }}>{pool.name}</h3>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{pool.protocol}</span>
        </div>
        {getRiskBadge(pool.id)}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginTop: '1.5rem' }}>
        <div>
          <div className="card-title">Raw APY</div>
          <div className="number-font" style={{ fontSize: '1.6rem', fontWeight: 700, color: '#fff' }}>
            {pool.rawApy ? parseFloat(pool.rawApy).toFixed(2) : '0.00'}%
          </div>
        </div>
        <div>
          <div className="card-title">Risk-Adjusted APY</div>
          <div className="number-font" style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--accent-btc)' }}>
            {pool.riskAdjustedApy ? parseFloat(pool.riskAdjustedApy).toFixed(2) : '0.00'}%
          </div>
        </div>
      </div>

      <div style={{ marginTop: '1.25rem', fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'flex', justifyContent: 'space-between' }}>
        <span>Optimized via AI rule-engine</span>
        <span>Last scan: {pool.lastUpdated ? new Date(pool.lastUpdated).toLocaleTimeString() : 'Just now'}</span>
      </div>
    </div>
  );
}
