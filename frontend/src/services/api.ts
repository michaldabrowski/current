import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Types
export interface Account {
  id: number;
  name: string;
  cashBalance: number;
  createdAt: string;
  updatedAt: string;
}

export interface Transaction {
  id: number;
  symbol: string;
  type: "BUY" | "SELL";
  assetType: "STOCK" | "CRYPTO";
  quantity: number;
  price: number;
  totalAmount: number;
  transactionDate: string;
  notes?: string;
}

export interface Holding {
  symbol: string;
  quantity: number;
  averagePrice: number;
  assetType: "STOCK" | "CRYPTO";
}

export interface CreateAccountRequest {
  name: string;
  initialBalance?: number;
}

export interface CreateTransactionRequest {
  accountId: number;
  symbol: string;
  type: "BUY" | "SELL";
  assetType: "STOCK" | "CRYPTO";
  quantity: number;
  price: number;
  notes?: string;
}

// API functions
export const accountsApi = {
  getAll: () => api.get<Account[]>("/accounts"),
  getById: (id: number) => api.get<Account>(`/accounts/${id}`),
  create: (data: CreateAccountRequest) => api.post<Account>("/accounts", data),
  delete: (id: number) => api.delete(`/accounts/${id}`),
};

export const transactionsApi = {
  getAll: () => api.get<Transaction[]>("/transactions"),
  getByAccount: (accountId: number) => api.get<Transaction[]>(`/transactions/account/${accountId}`),
  getHoldings: (accountId: number) => api.get<Holding[]>(`/transactions/holdings/${accountId}`),
  create: (data: CreateTransactionRequest) => api.post<Transaction>("/transactions", data),
};

export default api;
