import React, { useState, useEffect } from 'react';
import { ethers } from 'ethers';
import { connectUser, getUserPosition, triggerRebalance, getPools } from './services/api';
import Dashboard from './components/Dashboard';

function App() {
  const [wallet, setWallet] = useState('');
  const [user, setUser] = useState(null);
  const [position, setPosition] = useState(null);
  const [pools, setPools] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchPools();
    const interval = setInterval(fetchPools, 60000);
    return () => clearInterval(interval);
  }, []);

  const fetchPools = async () => {
    try { setPools(await getPools()); } catch (e) { console.error(e); }
  };

  const connectWallet = async () => {
    if (!window.ethereum) return alert('Please install MetaMask');
    try {
      const provider = new ethers.BrowserProvider(window.ethereum);
      const signer = await provider.getSigner();
      const address = await signer.getAddress();
      setWallet(address);
      const userData = await connectUser(address);
      setUser(userData);
      await fetchPosition(userData.id);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchPosition = async (userId) => {
    if (!userId) return;
    try {
      const data = await getUserPosition(userId);
      setPosition(data);
    } catch (e) { console.error(e); }
  };

  const handleRebalance = async () => {
    if (!wallet) return;
    setLoading(true);
    try {
      await triggerRebalance(wallet);
      await fetchPosition(user.id);
      alert('Rebalance check completed!');
    } catch (e) {
      alert('Rebalance failed');
    }
    setLoading(false);
  };

  return (
    <div className="app-container">
      <header>
        <h2>GOAT Network | BTCFi Agent</h2>
        {!wallet ? (
          <button className="btn-connect" onClick={connectWallet}>Connect Wallet</button>
        ) : (
          <div>Connected: {wallet.slice(0,6)}...{wallet.slice(-4)}</div>
        )}
      </header>

      {wallet && position ? (
        <Dashboard 
          position={position} 
          pools={pools} 
          onRebalance={handleRebalance} 
          loading={loading} 
        />
      ) : (
        <div className="card" style={{textAlign: 'center', padding: '3rem'}}>
          <h3>Connect your wallet to start optimizing your BTC yield!</h3>
        </div>
      )}
    </div>
  );
}

export default App;
