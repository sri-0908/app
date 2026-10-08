import React from 'react';

export default function WalletConnect({ walletAddress, isConnecting, onConnect, onDisconnect }) {
  // Helper to format wallet address e.g. 0x1234...5678
  const formatAddress = (addr) => {
    if (!addr) return '';
    return `${addr.substring(0, 6)}...${addr.substring(addr.length - 4)}`;
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
      {walletAddress ? (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div className="glass-panel" style={{ padding: '0.5rem 1rem', display: 'flex', alignItems: 'center', gap: '0.5rem', borderRadius: '10px' }}>
            <span className="pulse-dot"></span>
            <span className="number-font" style={{ fontSize: '0.9rem', fontWeight: 600 }}>
              {formatAddress(walletAddress)}
            </span>
          </div>
          <button className="btn-secondary" style={{ padding: '0.5rem 1rem', fontSize: '0.9rem' }} onClick={onDisconnect}>
            Disconnect
          </button>
        </div>
      ) : (
        <button 
          className="btn-primary pulse-glow" 
          onClick={onConnect} 
          disabled={isConnecting}
        >
          {isConnecting ? (
            <>
              <div className="loading-spinner"></div>
              Connecting...
            </>
          ) : (
            <>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                <path d="M21 18v1c0 1.1-.9 2-2 2H5c-1.11 0-2-.9-2-2V5c0-1.1.89-2 2-2h14c1.1 0 2 .9 2 2v1h-9c-1.11 0-2 .9-2 2v8c0 1.1.89 2 2 2h9zm-9-2h10V8H12v8zm4-2.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/>
              </svg>
              Connect Wallet
            </>
          )}
        </button>
      )}
    </div>
  );
}
