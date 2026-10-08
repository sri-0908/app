import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:8080/api' });

export const connectUser = async (walletAddress) => {
  const { data } = await API.post('/user/connect', { walletAddress });
  return data;
};

export const getUserPosition = async (userId) => {
  const { data } = await API.get(`/user/${userId}/position`);
  return data;
};

export const getPools = async () => {
  const { data } = await API.get('/pools');
  return data;
};

export const triggerRebalance = async (walletAddress) => {
  const { data } = await API.post('/agent/rebalance', { walletAddress });
  return data;
};
