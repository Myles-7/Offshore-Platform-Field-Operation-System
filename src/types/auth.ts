export interface LoginRequest {
  loginName: string;
  password: string;
  platform: 'PC' | 'MOBILE';
}

export interface CurrentUser {
  userId: number;
  username: string;
  realName: string;
  roleCodes: string[];
  permissionCodes: string[];
  dataScope: string;
  primaryProjectId?: number;
}

export interface LoginResponse extends CurrentUser {
  token: string;
}
