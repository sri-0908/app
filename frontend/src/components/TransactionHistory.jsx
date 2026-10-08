import React from 'react';

export default function TransactionHistory({ transactions }) {
  const formatTxHash = (hash) => {
    if (!hash) return '0x...';
    return `${hash.substring(0, 6)}...${hash.substring(hash.length - 4)}`;
  };

  const getPoolName = (poolId) => {
    if (!poolId) return 'None';
    if (poolId === 'babylon-btc') return 'Babylon';
    if (poolId === 'lorenzo-btc') return 'Lorenzo';
    if (poolId === 'pell-btc') return 'Pell Network';
    return poolId;
  };

  if (!transactions || transactions.length === 0) {
    return (
      <div className="glass-panel" style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-secondary)' }}>
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ marginBottom: '1rem', opacity: 0.5 }}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
        </svg>
        <div>No optimization transactions recorded yet.</div>
        <div style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>Your agent evaluates rebalance opportunities every 60 seconds.</div>
      </div>
    );
  }

  return (
    <div className="glass-panel" style={{ overflowX: 'auto', padding: '1rem' }}>
      <h3 style={{ margin: '0.5rem 0.5rem 1rem 0.5rem' }}>Agent Optimization Logs</h3>
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Type</th>
            <th>Route</th>
            <th>Amount</th>
            <th>Gas / Fee (GOAT)</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((tx) => (
            <tr key={tx.id}>
              <td className="number-font" style={{ whiteSpace: 'nowrap' }}>
                {new Date(tx.timestamp).toLocaleString()}
              </td>
              <td>
                <span className="badge badge-info">REBALANCE</span>
              </td>
              <td style={{ fontWeight: 600 }}>
                {getPoolName(tx.fromPool?.id || tx.fromPool)} 
                <span style={{ color: 'var(--accent-btc)', margin: '0 0.5rem' }}>➔</span> 
                {getPoolName(tx.toPool?.id || tx.toPool)}
              </td>
              <td className="number-font" style={{ fontWeight: 600 }}>
                {tx.amount ? parseFloat(tx.amount).toFixed(4) : '0.0000'} BTC
              </td>
              <td className="number-font" style={{ color: 'var(--text-secondary)' }}>
                {tx.fee ? (parseFloat(tx.fee) * 100000).toFixed(2) : '50.00'} GOAT
              </td>
              <td>
                <span className="badge badge-success">SETTLED</span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
