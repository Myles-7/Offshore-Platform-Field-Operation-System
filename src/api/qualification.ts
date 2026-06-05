import { request } from '@/api/request';
import type { CertificateVO, EmployeeVO, QualificationCheckVO } from '@/types/work-order';

/* ========== 人员资质 ========== */

/** 员工列表 */
export function fetchEmployees() {
  return request<EmployeeVO[]>({
    url: '/admin/employees',
    method: 'GET'
  });
}

/** 员工详情 */
export function fetchEmployee(id: number) {
  return request<EmployeeVO>({
    url: `/admin/employees/${id}`,
    method: 'GET'
  });
}

/** 员工证书列表 */
export function fetchEmployeeCertificates(employeeId: number) {
  return request<CertificateVO[]>({
    url: `/admin/employees/${employeeId}/certificates`,
    method: 'GET'
  });
}

/** 资质候选人检查 */
export function fetchQualificationCandidates(workOrderId: number) {
  return request<QualificationCheckVO[]>({
    url: `/admin/work-orders/${workOrderId}/qualification-candidates`,
    method: 'GET'
  });
}

/** 到期预警证书 */
export function fetchCertificateWarnings() {
  return request<CertificateVO[]>({
    url: '/admin/certificates/warnings',
    method: 'GET'
  });
}
