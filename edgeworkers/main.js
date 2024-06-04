import { httpRequest } from 'http-request';
import { createResponse } from 'create-response';

export async function responseProvider(request) {
    try {
        return httpRequest(`${request.scheme}://${request.host}${request.url}`).then(async response => { 
            try {
                const imageResizeUrl = "https://image-resizer.edgecloud9.com/resize";
                const contentType = response.getHeader('Content-Type');

                const options = {
                    method: 'POST',
                    headers: {
                        'user-agent': 'akamai',
                        'Content-Type': contentType
                    },
                    body: response.body
                };
                const resizedImageResponse = await httpRequest(imageResizeUrl, options);

                return createResponse(
                    200,
                    { 'Content-Type': contentType },
                    resizedImageResponse.body
                );
    
            } catch (exception) {
                return createResponse(
                    500,
                    { 'Content-Type': ['application/json'] },
                    JSON.stringify({ 
                        path: request.path,
                        error: exception,
                        errorMessage: exception.message,
                        stacktrace: exception.stack
                    })
                );
            }
        });
    } catch(exception) {
        return createResponse(
            500, {
                'Content-Type': ['application/json']
            },
            JSON.stringify({ "exception": exception.toString() })
        );
    }
}