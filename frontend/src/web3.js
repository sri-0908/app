import { ethers } from 'ethers';

// GOAT Network chain configuration parameters
const GOAT_NETWORK = {
  chainId: '0x139e', // 5022 in decimal (mock testnet chainId or live)
  chainName: 'GOAT Network Testnet',
  nativeCurrency: {
    name: 'GOAT Token',
    symbol: 'GOAT',
    decimals: 18,
  },
  rpcUrls: ['https://rpc.goat.network'],
  blockExplorerUrls: ['https://explorer.goat.network'],
};

/**
 * Checks if MetaMask is installed.
 */
export const hasMetaMask = () => {
  return typeof window !== 'undefined' && window.ethereum !== undefined;
};

/**
 * Connects to MetaMask and retrieves provider, signer, and wallet address.
 */
export const connectWallet = async () => {
  if (!hasMetaMask()) {
    throw new Error('MetaMask is not installed. Please install MetaMask and try again.');
  }

  try {
    // 1. Instantiate BrowserProvider (Ethers v6)
    const provider = new ethers.BrowserProvider(window.ethereum);
    
    // 2. Request account access
    const accounts = await window.ethereum.request({ method: 'eth_requestAccounts' });
    const walletAddress = accounts[0];
    
    // 3. Get Signer
    const signer = await provider.getSigner();

    // 4. Switch to GOAT Network or add it if missing
    await ensureGoatNetwork();

    return {
      provider,
      signer,
      walletAddress,
    };
  } catch (error) {
    console.error('Wallet connection failed:', error);
    throw error;
  }
};

/**
 * Prompt the user to switch to GOAT Network, or add it if not configured.
 */
export const ensureGoatNetwork = async () => {
  if (!window.ethereum) return;

  try {
    // Attempt to switch to the GOAT Network chain ID
    await window.ethereum.request({
      method: 'wallet_switchEthereumChain',
      params: [{ chainId: GOAT_NETWORK.chainId }],
    });
  } catch (switchError) {
    // If chain is not added (error code 4902), request to add it
    if (switchError.code === 4902) {
      try {
        await window.ethereum.request({
          method: 'wallet_addEthereumChain',
          params: [GOAT_NETWORK],
        });
      } catch (addError) {
        console.error('Failed to add GOAT Network to MetaMask:', addError);
        throw addError;
      }
    } else {
      console.error('Failed to switch to GOAT Network:', switchError);
      throw switchError;
    }
  }
};

/**
 * Sign a cryptographic message using the user's private key to authorize the agent registration.
 */
export const signAgentAuthorization = async (signer, agentAddress) => {
  if (!signer) {
    throw new Error('Signer is required to sign authorization message');
  }

  const message = `Authorize BTCFi Yield Optimizer Agent to manage assets and execute rebalances.\n\nAgent Address: ${agentAddress}\nTimestamp: ${Date.now()}`;
  
  try {
    const signature = await signer.signMessage(message);
    return {
      message,
      signature,
    };
  } catch (error) {
    console.error('Signing authorization failed:', error);
    throw error;
  }
};
