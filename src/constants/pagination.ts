import type { PageParams } from '@/types/api';

export const defaultPageParams: PageParams = {
  pageNum: 1,
  pageSize: 10
};

export const pageSizeOptions = [10, 20, 50, 100, 200];
