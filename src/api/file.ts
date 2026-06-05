import { request } from '@/api/request';

/* ========== 文件操作 ========== */

export function fetchFilePreviewUrl(fileId: string): string {
  return `/api/files/${fileId}/preview`;
}

export function fetchFileDownloadUrl(fileId: string): string {
  return `/api/files/${fileId}/download`;
}

export function deleteFile(fileId: string) {
  return request<void>({
    url: `/api/files/${fileId}`,
    method: 'DELETE'
  });
}
