export interface MaterialItem {
  id: number;
  materialCode: string;
  materialName: string;
  materialSpec?: string;
  unit?: string;
  materialType?: string;
  safetyStock?: number;
  currentStock?: number;
}
