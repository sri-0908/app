import React from 'react';

export default function Dashboard({ position, pools, onRebalance, loading }) {
  return (
    <div>
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3>Your Yield Portfolio</h3>
          <button 
            className="btn-connect" 
            onClick={onRebalance} 
            disabled={loading}
          >
            {loading ? 'Rebalancing...' : 'Manual AI Rebalance'}
          </button>
        </div>
        <div className="grid">
          <div className="stat-box">
            <div>Current Pool</div>
            <div className="stat-value">{position.currentPool || 'None'}</div>
            <div style={{color: '#4ade80'}}>{position.currentApy}% APY</div>
          </div>
          <div className="stat-box">
            <div>Total Earnings</div>
            <div className="stat-value">{position.totalEarnings.toFixed(6)} BTC</div>
          </div>
          <div className="stat-box">
            <div>Total Rebalances</div>
            <div className="stat-value">{position.totalRebalances}</div>
            {position.lastRebalanceAt && (
              <div style={{fontSize: '0.8rem', color: '#999'}}>
                Last: {new Date(position.lastRebalanceAt).toLocaleString()}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="card">
        <h3>Available GOAT Network Pools</h3>
        <table className="pools-table">
          <thead>
            <tr>
              <th>Pool Name</th>
              <th>Protocol</th>
              <th>Risk</th>
              <th>TVL</th>
              <th>Current APY</th>
            </tr>
          </thead>
          <tbody>
            {pools.map(p => (
              <tr key={p.id} style={{background: p.isBestPool ? '#2c2c2c' : 'transparent'}}>
                <td>
                  {p.name}
                  {p.isBestPool && <span className="best-badge">AI Target</span>}
                </td>
                <td>{p.protocol}</td>
                <td style={{color: p.riskScore <= 3 ? '#4ade80' : p.riskScore <= 7 ? '#fbbf24' : '#f87171'}}>
                  {p.riskLabel}
                </td>
                <td>${(p.totalValueLocked / 1000000).toFixed(2)}M</td>
                <td style={{color: '#F7931A', fontWeight: 'bold'}}>{p.apy}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h3>Agent Activity History</h3>
        {position.recentTransactions?.length === 0 ? (
          <p>No activity yet.</p>
        ) : (
          <ul className="tx-list">
            {position.recentTransactions?.map(tx => (
              <li key={tx.id} className="tx-item">
                <div>
                  <strong>Moved to {tx.toPool}</strong>
                  <div style={{fontSize: '0.8rem', color: '#999'}}>
                    {new Date(tx.timestamp).toLocaleString()}
                  </div>
                </div>
                <div style={{textAlign: 'right'}}>
                  <div className={tx.status === 'SUCCESS' ? 'success' : 'error'}>
                    {tx.status}
                  </div>
                  <div style={{fontSize: '0.8rem'}}>
                    APY: {tx.apyBefore}% → {tx.apyAfter}%
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
