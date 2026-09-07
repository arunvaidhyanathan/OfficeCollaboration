const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export type Permission = 'owner' | 'editor' | 'viewer' | string;

export interface DocumentSummary {
  id: string;
  name: string;
  mimeType: string;
  updatedAt: string;
  permission: Permission;
}

/** Loose type for the ONLYOFFICE editor config - shape is defined by the backend/ONLYOFFICE, not us. */
export type OnlyOfficeConfig = Record<string, unknown>;

class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, init);
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ApiError(res.status, text || `Request failed with status ${res.status}`);
  }
  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}

export async function listDocuments(): Promise<DocumentSummary[]> {
  return request<DocumentSummary[]>('/documents', { method: 'GET' });
}

export async function uploadDocument(file: File, name: string): Promise<DocumentSummary> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('name', name);

  const res = await fetch(`${API_BASE_URL}/documents`, {
    method: 'POST',
    // do NOT set Content-Type - the browser sets the multipart boundary
    body: formData,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ApiError(res.status, text || `Upload failed with status ${res.status}`);
  }
  return (await res.json()) as DocumentSummary;
}

export async function createBlankDocument(
  name: string,
  type: 'docx' | 'xlsx' | 'pptx'
): Promise<DocumentSummary> {
  return request<DocumentSummary>('/documents/new', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, type }),
  });
}

export async function getEditorConfig(documentId: string): Promise<OnlyOfficeConfig> {
  return request<OnlyOfficeConfig>(`/documents/${documentId}/editor-config`, { method: 'GET' });
}

export async function renameDocument(documentId: string, name: string): Promise<DocumentSummary> {
  return request<DocumentSummary>(`/documents/${documentId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  });
}

/** Raw file bytes, proxied by the backend (see OnlyOfficeCallbackController#content) -- not under /documents. */
export function downloadUrl(documentId: string): string {
  return `${API_BASE_URL}/onlyoffice/content?documentId=${documentId}`;
}
