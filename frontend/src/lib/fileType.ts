import { FileText, Table, Presentation, File as FileIcon, type LucideIcon } from 'lucide-react';

export type FileKind = 'docx' | 'xlsx' | 'pptx' | 'other';

interface FileTypeInfo {
  kind: FileKind;
  label: string;
  icon: LucideIcon;
  colorClass: string;
  bgClass: string;
}

const DOCX: FileTypeInfo = { kind: 'docx', label: 'Word', icon: FileText, colorClass: 'text-word', bgClass: 'bg-word/10' };
const XLSX: FileTypeInfo = { kind: 'xlsx', label: 'Sheet', icon: Table, colorClass: 'text-sheet', bgClass: 'bg-sheet/10' };
const PPTX: FileTypeInfo = { kind: 'pptx', label: 'Slides', icon: Presentation, colorClass: 'text-slide', bgClass: 'bg-slide/10' };
const OTHER: FileTypeInfo = { kind: 'other', label: 'File', icon: FileIcon, colorClass: 'text-gray-500', bgClass: 'bg-gray-100' };

export function fileTypeOf(name: string): FileTypeInfo {
  const ext = name.split('.').pop()?.toLowerCase();
  if (ext === 'docx') return DOCX;
  if (ext === 'xlsx') return XLSX;
  if (ext === 'pptx') return PPTX;
  return OTHER;
}
