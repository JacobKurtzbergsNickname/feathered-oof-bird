export type AuthRole = 'PERSONAL' | 'BUSINESS' | 'ADMIN';
export type AuthStatus = 'ACTIVE' | 'RESTRICTED' | 'SUSPENDED';

export interface AuthUser {
  id: number;
  email: string;
  role: AuthRole;
  status: AuthStatus;
}

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  user: AuthUser;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  role: Exclude<AuthRole, 'ADMIN'>;
}