export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
  traceId?: string;
}

export interface PageParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  sortField?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface RequestOptions {
  showError?: boolean;
}
