export const appendJsonData = (formData: FormData, payload: Record<string, unknown>) => {
    try {
        const plainPayload = Object.fromEntries(
            Object.entries(payload).filter(([, value]) => value !== undefined)
        );
        const json = JSON.stringify(plainPayload);
        // Backend expects `data` as JSON request-part.
        formData.append('data', new Blob([json], { type: 'application/json' }), 'data.json');

        if (import.meta.env.DEV) {
            console.debug('[multipart] data payload prepared', plainPayload);
        }
    } catch (error) {
        if (import.meta.env.DEV) {
            console.error('[multipart] failed to stringify data payload', payload, error);
        }
        throw new Error('FORM_DATA_JSON_ERROR');
    }
};

export const isPdfFile = (file: File | undefined | null) => {
    if (!file) return false;
    const typeOk = file.type === 'application/pdf';
    const nameOk = file.name.toLowerCase().endsWith('.pdf');
    return typeOk || nameOk;
};
