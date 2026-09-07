import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useNavigate } from 'react-router-dom';
import { Search, LayoutGrid, List as ListIcon, UploadCloud } from 'lucide-react';
import {
  listDocuments,
  uploadDocument,
  createBlankDocument,
  renameDocument,
  type DocumentSummary,
} from '../lib/api';
import { fileTypeOf } from '../lib/fileType';
import Sidebar, { type FilterValue } from '../components/Sidebar';
import DocumentCard from '../components/DocumentCard';
import DocumentRow from '../components/DocumentRow';

type ViewMode = 'grid' | 'list';

export default function DocumentList() {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [documents, setDocuments] = useState<DocumentSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [filter, setFilter] = useState<FilterValue>('all');
  const [query, setQuery] = useState('');
  const [view, setView] = useState<ViewMode>('grid');

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      setDocuments(await listDocuments());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function handleUpload(file: File) {
    setBusy(true);
    setError(null);
    try {
      const doc = await uploadDocument(file, file.name);
      navigate(`/documents/${doc.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setBusy(false);
    }
  }

  function handleFileSelected(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (file) handleUpload(file);
  }

  async function handleCreateBlank(type: 'docx' | 'xlsx' | 'pptx') {
    const defaultName =
      type === 'docx' ? 'Untitled document' : type === 'xlsx' ? 'Untitled spreadsheet' : 'Untitled presentation';
    const name = window.prompt('Document name', defaultName);
    if (!name) return;

    setBusy(true);
    setError(null);
    try {
      const doc = await createBlankDocument(name, type);
      navigate(`/documents/${doc.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create document');
    } finally {
      setBusy(false);
    }
  }

  async function handleRename(id: string, newName: string) {
    const previous = documents;
    setDocuments((docs) => docs.map((d) => (d.id === id ? { ...d, name: newName } : d)));
    try {
      await renameDocument(id, newName);
    } catch (err) {
      setDocuments(previous);
      setError(err instanceof Error ? err.message : 'Rename failed');
    }
  }

  const onDrop = useCallback((accepted: File[]) => {
    if (accepted[0]) handleUpload(accepted[0]);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    noClick: true,
    accept: {
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'application/vnd.openxmlformats-officedocument.presentationml.presentation': ['.pptx'],
      'application/pdf': ['.pdf'],
    },
  });

  const filtered = useMemo(() => {
    return documents
      .filter((d) => filter === 'all' || fileTypeOf(d.name).kind === filter)
      .filter((d) => d.name.toLowerCase().includes(query.toLowerCase()));
  }, [documents, filter, query]);

  return (
    <div className="flex h-screen flex-col">
      <header className="flex h-14 shrink-0 items-center justify-between border-b border-gray-200 px-4">
        <div className="flex items-center gap-2 font-semibold text-gray-800">
          <span className="text-blue-600">🟦</span>
          LocalCollab
        </div>
        <div className="relative w-full max-w-md">
          <Search size={15} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search documents..."
            className="w-full rounded-lg border border-gray-200 bg-gray-50 py-1.5 pl-9 pr-3 text-sm outline-none focus:border-blue-400 focus:bg-white"
          />
        </div>
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-xs font-semibold text-blue-700">
          LU
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        <Sidebar
          filter={filter}
          onFilterChange={setFilter}
          onCreateBlank={handleCreateBlank}
          onUploadClick={() => fileInputRef.current?.click()}
          busy={busy}
        />
        <input ref={fileInputRef} type="file" accept=".docx,.xlsx,.pptx,.pdf" onChange={handleFileSelected} className="hidden" />

        <main {...getRootProps()} className="relative flex-1 overflow-y-auto p-6">
          <input {...getInputProps()} />

          {isDragActive && (
            <div className="pointer-events-none absolute inset-0 z-10 flex items-center justify-center border-2 border-dashed border-blue-400 bg-blue-50/90">
              <div className="flex flex-col items-center gap-2 text-blue-600">
                <UploadCloud size={36} />
                <p className="font-medium">Drop files here to upload</p>
              </div>
            </div>
          )}

          <div className="mb-4 flex items-center justify-between">
            <h1 className="text-lg font-semibold text-gray-800">My Documents</h1>
            <div className="flex rounded-md border border-gray-200 p-0.5">
              <button
                type="button"
                onClick={() => setView('grid')}
                className={`rounded p-1.5 ${view === 'grid' ? 'bg-gray-100 text-gray-800' : 'text-gray-400'}`}
                aria-label="Grid view"
              >
                <LayoutGrid size={15} />
              </button>
              <button
                type="button"
                onClick={() => setView('list')}
                className={`rounded p-1.5 ${view === 'list' ? 'bg-gray-100 text-gray-800' : 'text-gray-400'}`}
                aria-label="List view"
              >
                <ListIcon size={15} />
              </button>
            </div>
          </div>

          {error && <p className="mb-3 text-sm text-red-600">{error}</p>}

          {loading ? (
            <p className="text-sm text-gray-500">Loading...</p>
          ) : filtered.length === 0 ? (
            <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-gray-200 py-16 text-center text-gray-400">
              <UploadCloud size={32} className="mb-2" />
              <p className="text-sm">No documents yet. Drag a file here, or use New to get started.</p>
            </div>
          ) : view === 'grid' ? (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
              {filtered.map((doc) => (
                <DocumentCard
                  key={doc.id}
                  doc={doc}
                  onOpen={() => navigate(`/documents/${doc.id}`)}
                  onRename={(name) => handleRename(doc.id, name)}
                />
              ))}
            </div>
          ) : (
            <div className="overflow-hidden rounded-lg border border-gray-200">
              {filtered.map((doc) => (
                <DocumentRow
                  key={doc.id}
                  doc={doc}
                  onOpen={() => navigate(`/documents/${doc.id}`)}
                  onRename={(name) => handleRename(doc.id, name)}
                />
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
